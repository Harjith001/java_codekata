package org.example.echo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class MultiClientEchoServer implements EchoServer {
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    @Override
    public void start(int port) throws IOException, SocketException {
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

    @Override
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    public static void main(String[] args) {
        int port = 5001;
        MultiClientEchoServer server = new MultiClientEchoServer();

        Thread serverThread = new Thread(() -> {
            try {
                server.start(port);
            } catch (IOException e) {
                System.err.println("Server error: " + e.getMessage());
            }
        });
        serverThread.start();
    }
}
