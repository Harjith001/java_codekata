package org.example.playground;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiPoolServer {
    private static final int THREAD_COUNT = 10; // Example thread pool size

    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        ServerSocket serverSocket = new ServerSocket(5001);
        System.out.println("Server listening at port 5001");

        while (true) {
            Socket connectionSocket = serverSocket.accept();
            threadPool.submit(() -> {
                try (Socket socket = connectionSocket; // auto-close on completion
                     DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                     DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream())) {

                    String messageReceived = dataIn.readUTF();
                    System.out.println("Client's message: " + messageReceived);
                    dataOut.writeUTF("Server's response: " + messageReceived);

                } catch (IOException e) {
                    System.err.println("Connection error: " + e.getMessage());
                }
            });
        }
    }
}
