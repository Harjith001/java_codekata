package org.example.echo;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiClient {

    private static final int COUNT = 100000000;

    public static void sendMessage(String message, int port) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", port);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer.write(message + "\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println("Received from server: " + response);

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }

    }

    public static void main(String[] args) {


//        for (int i = 0; i < COUNT; i++) {
//            final int clientId = i;
//            new Thread(() -> {
//                try {
//                    sendMessage("Hello from client " + clientId + "\n", 5001);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }).start();
//        }
        MultiClient client = new MultiClient();
        client.threadPoolImplementation();
    }

    private void threadPoolImplementation() {
        ExecutorService threadPool = Executors.newFixedThreadPool(10000);

        for (int i = 0; i < COUNT; i++) {
            final int clientId = i;
            threadPool.submit(() -> {
                try {
                    sendMessage("Hello from client " + clientId + "\n", 5001);
                } catch (IOException e) {
                    System.err.println("Error sending message from client " + clientId + ": " + e.getMessage());
                }
            });
        }

        threadPool.shutdown();
    }

}
