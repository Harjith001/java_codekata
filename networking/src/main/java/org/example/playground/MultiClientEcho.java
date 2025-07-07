package org.example.playground;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiClientEcho {
    private ServerSocket serverSocket;
    private volatile boolean running = true;


    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server listening at port " + port);

        while (running) {
            Socket connectionSocket = serverSocket.accept();
            new Thread(() -> {
                try (DataInputStream dataIn = new DataInputStream(connectionSocket.getInputStream());
                     DataOutputStream dataOut = new DataOutputStream(connectionSocket.getOutputStream())) {

                    String messageReceived = dataIn.readUTF();
                    System.out.println("Client's message: " + messageReceived);
                    dataOut.writeUTF("Server's " + messageReceived);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }


    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}
