package main.server.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private int port = 8888;
    private ServerSocket serverSocket;
    public static long tempLastModified = 0;

    public Server(){
        try {
            this.serverSocket = new ServerSocket(this.port);

            // start listening
            while (true){
                System.out.println("Listening...");
                Socket socket = this.serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket);
                clientThread.start();
                System.out.println("new connection started");
            }

        } catch (IOException e) {
            System.out.println("can't start the server: " + e.getMessage());
        }

    }
}
