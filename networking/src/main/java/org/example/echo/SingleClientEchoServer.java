package org.example.echo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SingleClientEchoServer implements EchoServer {

    private ServerSocket serverSocket;
    private volatile boolean running = true;

    @Override
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server listening at port "+ port);


        Socket connectionSocket = serverSocket.accept();

        while(running) {

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
            }
        }
    }

    @Override
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 5001;
        SingleClientEchoServer server = new SingleClientEchoServer();
        server.start(port);
    }
}
