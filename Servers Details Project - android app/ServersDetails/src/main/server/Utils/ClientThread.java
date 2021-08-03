package main.server.Utils;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class ClientThread extends Thread {

    private Socket socket;
    private DataInputStream input;

    private DataOutputStream output;

    private String filePath = "D:\\Users\\kinsh\\Desktop\\Servers Details Project\\Servers Details Project\\servers.txt";

    public ClientThread (Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            // init input and output objects
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // do the security check by checking the security key
            if (this.securityCheck()) {
                while (true) {
                    try {
                        // get the action from client
                        String msg = this.input.readUTF();

                        if (msg != null) {
                            // do the action client asked to
                            doAction(msg);
                        }
                    } catch (Exception e) {
                        System.out.println("connection with client stoped: " + e.getMessage());
                        input.close();
                        output.close();
                        this.socket.close();
                        break;
                    }
                }

                input.close();
                output.close();
                this.socket.close();
            } else {
                System.out.println("ALERT - Connection with WRONG security key tried to connect");
                socket.close();
            }

        } catch (IOException e) {
            System.out.println("can't create input/output to client");
            try {
                this.socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    // method that take the msg and do an action
    private void doAction(String msg) throws IOException {
        switch (msg){
            // client has asked for details
            case Protocol.ASKFORDETAILS:
                boolean done = false;
                StringBuilder stringBuilder = new StringBuilder();
                String txtFileStr = this.getTxtFileContent();



                try {
                    this.output.writeUTF(txtFileStr);
                } catch (IOException e) {
                    System.out.println("can't send string to client: " + e.getMessage());
                }

            break;

            case Protocol.CLEANLASTMODIFIED:
                Server.tempLastModified = 0;
                break;
        }
    }

    // encryption
    private byte[] encryptText(String msg) throws UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        // text encryption
        // encrypt text
        String charSet = "UTF-8";
        String key = "SecretKey1234567"; // 128 bit key
        // Create key and cipher
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        // encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(msg.getBytes(charSet));
    }


    //method to get the txt from the file
    private String getTxtFileContent(){
        FileReader fr = null;
        StringBuilder stringBuilder = new StringBuilder();

        File txtFile = new File(filePath);
        long fileLastTimeModifed = txtFile.lastModified();

        if (fileLastTimeModifed == Server.tempLastModified){
            try {
                output.writeUTF("NOTCHANGED");
                output.writeUTF(String.valueOf(fileLastTimeModifed));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Server.tempLastModified = fileLastTimeModifed;
            try {
                output.writeUTF("CHANGED");
                // send txt file last time modifed to client
                output.writeUTF(String.valueOf(fileLastTimeModifed));

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(filePath), "UTF-8"));

                String str;

                while ((str = in.readLine()) != null) {
                    stringBuilder.append(str + "\n");
                }

                in.close();
                System.out.println("done reading the txt file");

            } catch (FileNotFoundException e) {
                System.out.println("file not found: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("error reading the txt file: " + e.getMessage());
            }
        }

        return stringBuilder.toString();
    }

    private boolean securityCheck(){
        try {
            output.writeUTF(Protocol.ASKFORSECRETKEY);
            String str = input.readUTF();
            if (str.equals(Protocol.SECRETKEY)) {
                return true;
            }
        } catch (IOException e) {
            System.out.println("can't read from client: " + e.getMessage());
        }
        return false;
    }
}