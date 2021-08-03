package com.ilan.screenshare.serversdetails;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangeIpActivity extends AppCompatActivity {

    EditText txtServerIp;
    EditText txtServerPort;

    Button btnSaveServerIp;
    Button btnCloseWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_ip);

        SharedPreferences sharedPreferences = getSharedPreferences("ServerDetails", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        String serverIp = sharedPreferences.getString("ServerIp", "");


        // put in the plain text the current server ip
        txtServerIp = (EditText) findViewById(R.id.txtServerIp);
        txtServerIp.setText(sharedPreferences.getString("ServerIp", "ip not defined yet"));

        txtServerPort = (EditText) findViewById(R.id.txtServerPort);
        txtServerPort.setText(sharedPreferences.getString("ServerPort", "8888"));

        // button save server ip
        btnSaveServerIp = (Button) findViewById(R.id.btnSaveServerIp);
        btnSaveServerIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtServerIp.getText().toString() != null && txtServerPort.getText().toString() != null) {
                    editor.putString("ServerIp", txtServerIp.getText().toString());
                    editor.putString("ServerPort", txtServerPort.getText().toString());
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "details saved", Toast.LENGTH_LONG).show();
                }
            }
        });

        // button close window
        btnCloseWindow = (Button) findViewById(R.id.btnCloseWindow);
        btnCloseWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
