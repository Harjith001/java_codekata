package org.example.echo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MultiClientEchoServer implements EchoServer {
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private final Set<Socket> activeConnections = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server listening at port " + port);

        while (running) {
            try {
                Socket connectionSocket = serverSocket.accept();

                if(!running){
                    connectionSocket.close();
                    break;
                }

                activeConnections.add(connectionSocket);

                new Thread(() -> {
                    try (
                            InputStream inputStream = connectionSocket.getInputStream();
                            OutputStream outputStream = connectionSocket.getOutputStream()
                    ) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        StringBuilder messageBuilder = new StringBuilder();

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            String part = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                            messageBuilder.append(part);

                            int newlineIndex;
                            while ((newlineIndex = messageBuilder.indexOf("\n")) != -1) {
                                String message = messageBuilder.substring(0, newlineIndex).trim();
                                messageBuilder.delete(0, newlineIndex + 1);

                                System.out.println("Client's message: " + message);

                                if ("exit".equalsIgnoreCase(message)) {
                                    outputStream.write("Goodbye!\n".getBytes(StandardCharsets.UTF_8));
                                    outputStream.flush();
                                    return;
                                }

                                String response = "Server's " + message + "\n";
                                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                                outputStream.flush();
                            }
                        }
                        System.out.println("Client disconnected");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        activeConnections.remove(connectionSocket);
                        try {
                            connectionSocket.close();
                        } catch (IOException e) {
                            // log
                        }
                    }
                }).start();
            } catch (IOException e ) {
                System.out.println("Client connection error: " + e.getMessage());
            }
        }

        System.out.println("Server has stopped.");
    }

    @Override
    public void stop() throws IOException {
        running = false;

        for(Socket socket: activeConnections) {
            socket.close();
        }
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
                Thread.sleep(15000); // 30 seconds
                System.out.println("Server is terminated : ");
                server.stop();
                //System.exit(0);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }
}
