package org.example.echo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class MultiClientEchoServer implements EchoServer {
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    @Override
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server listening at port " + port);

        while (running) {
            try {
                Socket connectionSocket = serverSocket.accept();
                new Thread(() -> {
                    try (
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(connectionSocket.getInputStream(), StandardCharsets.UTF_8));
                            BufferedWriter writer = new BufferedWriter(
                                    new OutputStreamWriter(connectionSocket.getOutputStream(), StandardCharsets.UTF_8))
                    ) {
                        String messageReceived;
                        while ((messageReceived = reader.readLine()) != null) {
                            System.out.println("Client's message: " + messageReceived);

                            if ("exit".equalsIgnoreCase(messageReceived.trim())) {
                                writer.write("Goodbye!\n");
                                writer.flush();
                                break;
                            }
                            writer.write("Server's " + messageReceived + "\n");
                            writer.flush();
                        }
                        System.out.println("Client disconnected");
                    } catch (IOException e) {
                        System.out.println("Client connection error: " + e.getMessage());
                    }
                }).start();
            } catch (IOException e ) {
                // log
            }
        }

        System.out.println("Server has stopped.");
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
        new Thread(() -> {
            try {
                server.start(port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(30000); // 30 seconds
                System.out.println("Server is terminated : ");
                server.stop();
                //System.exit(0);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
