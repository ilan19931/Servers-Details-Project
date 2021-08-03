package com.ilan.screenshare.serversdetails;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ilan.screenshare.serversdetails.Utils.Helper;
import com.ilan.screenshare.serversdetails.Utils.LoadingDialog;
import com.ilan.screenshare.serversdetails.Utils.Protocol;
import com.ilan.screenshare.serversdetails.Utils.RecyclerViewAdapter;
import com.ilan.screenshare.serversdetails.Utils.ServerDetail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    Socket socket = null;

    private DataInputStream input;
    private DataOutputStream output;

    static Handler handler = null;

    private String TAG = "AppLog";

    private ArrayList<ServerDetail> listOfServers;
    private ArrayList<ServerDetail> tempListOfServers;

    RecyclerView recyclerView;
    RecyclerViewAdapter myAdapter;

    private LoadingDialog loadingDialog;

    TextView txtFileLastTimeModifed;

    Timer timer = null;
    int timerCnt = 300;
    int timerTime = 3000;
    boolean error = false;
    boolean firstTime = true;
    String[] detailsOfLastModifed;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtFileLastTimeModifed = (TextView) findViewById(R.id.txtFileLastTimeModifed);

        final Button btnAskForDetails = (Button) findViewById(R.id.btnAskForDetails);

        listOfServers = new ArrayList<ServerDetail>(); // init list of servers with servers details
        tempListOfServers = new ArrayList<ServerDetail>(); // init temp list of servers with servers details

        // init recyclerview and his adapter
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        myAdapter = new RecyclerViewAdapter(getApplicationContext(), listOfServers);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        recyclerView.setAdapter(myAdapter);

        // handler to change recyclerview adapdet in main thread
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==10){
                    if( !listOfServers.isEmpty() && listOfServers != null && myAdapter != null && listOfServers.size() > 0)
                        myAdapter.notifyDataSetChanged();
                }
                if(msg.what == 5){
                    btnAskForDetails.setClickable(true);
                }

                if(msg.what == 6){

                    recyclerView.stopScroll();
                    recyclerView.stopNestedScroll();

                    // create loading dialog
                    loadingDialog = new LoadingDialog(MainActivity.this);
                    loadingDialog.showDialog();
                }
                if(msg.what == 7){
                    loadingDialog.hideDialog();
                }

                super.handleMessage(msg);
            }
        };

        //button ask deatils
        btnAskForDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAskForDetails.setClickable(false);
                timerCnt = 300;

                // create data grid first time
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        createDataGrid();
                    }
                }).start();

                // set the timer and the task within the timer
                timer = new Timer();
                TimerTask myTask = new TimerTask() {
                    @Override
                    public void run() {
                        createDataGrid();
                        timerCnt -= 3;
                        if(timerCnt <= 0 || error) {
                            timer.cancel();
                            // return btn to clickable
                            Message msg = handler.obtainMessage();
                            msg.what = 5;
                            handler.sendMessage(msg);
                            Log.d(TAG, "timer schedule done");
                        }
                    }
                };
                timer.schedule(myTask, timerTime, timerTime);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // do actions when menu item clicked
        switch (id){
            // change server settings option
            case R.id.action_settings:
                Intent intent = new Intent(this, ChangeIpActivity.class);
                startActivity(intent);
                break;

            // Stop checking option
            case R.id.action_StopChecking:
                if (timer != null)
                    timer.cancel();
                // return btn to clickable
                Message msg = handler.obtainMessage();
                msg.what = 5;
                handler.sendMessage(msg);
                break;

            //close app option
            case R.id.action_closeApp:
                finish();
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createDataGrid() {

        boolean done = false;
        error = false;

        // get server ip and port from shared pref
        SharedPreferences sharedPreferences = getSharedPreferences("ServerDetails", Context.MODE_PRIVATE);
        String host = sharedPreferences.getString("ServerIp", "");
        int port = Integer.parseInt(sharedPreferences.getString("ServerPort", "8888"));

        try {
            // connect to server
            Log.d(TAG, "trying to connect");
            socket = new Socket(host, port);
            Log.d(TAG, "connected to server");

            // init input and output objects
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // ask the server for details
            askForDetails();

            done = true;
            error = false;

        } catch (IOException e) {
            Log.d(TAG, "error: " + e.getMessage());
            error = true;
            // close thread
            Thread.currentThread().interrupt();
            return;
        }

        // wait untill all data will come
        if (!error) {
            while (!done) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (error) {
                    Toast.makeText(getApplicationContext(), "Can't connect to server", Toast.LENGTH_LONG).show();
                    timer.cancel();
                    break;
                }
            }
            //close connection
            try {
                input.close();
                output.close();
                socket.close();
            } catch (IOException e) {
                Log.d(TAG,"IO error: " + e.getMessage());
            }
        } else {
            Toast.makeText(getApplicationContext(), "Can't connect to server", Toast.LENGTH_LONG).show();
        }
        Message msg = handler.obtainMessage();
        msg.what = 10;
        handler.sendMessage(msg);

    }

    // put all server into arraylist
    private void putServersIntoList(String txt) throws UnsupportedEncodingException {

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        listOfServers.clear();
        String[] servers = txt.split("\n");

        for (String server : servers){
            if (server == null || server.equals("\n") || server.equals("\r") || server.equals("") || server.isEmpty())
                continue;

            int commaPosition = server.indexOf(',');
            String name = server.substring(0, commaPosition);
            // check if status is a number
            if (!server.substring(commaPosition + 1, commaPosition + 2).matches("\\d+"))
                continue;

            int status = Integer.parseInt(server.substring(commaPosition + 1, commaPosition + 2));
            ServerDetail serverDetail = new ServerDetail(name, status);
            this.listOfServers.add(serverDetail);
        }

    }


    private void doSecurityCheckWithServer() throws IOException {
        String str = input.readUTF();
        if (str.equals(Protocol.ASKFORSECRETKEY)){
            output.writeUTF(Protocol.SECRETKEY);
        }
    }

    private void askForDetails() throws IOException {
        try {
            //do security check with server
            doSecurityCheckWithServer();

            if (firstTime) {
                firstTime = false;
                try {
                    output.writeUTF(Protocol.CLEANLASTMODIFIED);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            output.writeUTF(Protocol.ASKFORDETAILS);
            String changed = input.readUTF();

            // get last modified data
            String fileLastTimeModifed = input.readUTF();
            String msg = input.readUTF();
            String str = Helper.getTimeDifferrence(fileLastTimeModifed);
            detailsOfLastModifed = str.split("@");
            Log.d(TAG, "done reading txt file");

            if(changed.equals("NOTCHANGED")) {
                updateLastModifiedTextView();
            }
            else {

                //Decrypt the text
                String decryptedText = decryptText(msg);


                Message msg2 = handler.obtainMessage();
                msg2.what = 6;
                handler.sendMessage(msg2);


                updateLastModifiedTextView();

                putServersIntoList(decryptedText);

                Message msg3 = handler.obtainMessage();
                msg3.what = 7;
                handler.sendMessage(msg3);
            }
        } catch (IOException e) {
            Log.d(TAG,"askfordetails error: " + e.getMessage());
        } catch (ParseException e) {
            Log.d(TAG, "something wrong with last modifed date: " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }

    private void updateLastModifiedTextView(){
        // set textview that holds last modifed details
        txtFileLastTimeModifed.setText("עדכון אחרון בוצע לפניי: \n" + detailsOfLastModifed[0]);
        // set the color of last time modifed textview bg color
        if (detailsOfLastModifed[1].equals("1"))
            txtFileLastTimeModifed.setBackgroundColor(Color.RED);
        else
            txtFileLastTimeModifed.setBackgroundColor(Color.LTGRAY);
    }


    // decryption method
    private String decryptText(String msg) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        String charSet = "UTF-8";
        String key = "SecretKey1234567"; // 128 bit key
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        try {
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
        } catch (InvalidKeyException e) {
            Log.d("AppLog", "decryption key is invalid: " + e.getMessage());
        }
        String decryptedText=null;
        byte[] decodedValue = Base64.decode(msg.toString(), Base64.DEFAULT);
        byte[] plainText = cipher.doFinal(decodedValue);
        decryptedText= new String(plainText);

        return decryptedText;
    }
}
