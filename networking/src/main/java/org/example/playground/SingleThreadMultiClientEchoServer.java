package org.example.playground;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.echo.EchoServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SingleThreadMultiClientEchoServer implements EchoServer {

    private static final Logger LOG = LogManager.getLogger(SingleThreadMultiClientEchoServer.class);

    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private final List<ClientConnection> clients = new ArrayList<>();

    private static class ClientConnection {
        Socket socket;
        InputStream inputStream;
        OutputStream outputStream;
        StringBuilder messageBuilder;

        ClientConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            this.messageBuilder = new StringBuilder();
        }

        void close() {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.out.println("Error closing client: " + e.getMessage());
            }
        }
    }

    @Override
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port, 200);

        serverSocket.setSoTimeout(50);
        System.out.println("Server listening at port " + port);

        while (running) {
            try {
                Socket newClient = serverSocket.accept();
                ClientConnection clientHandler = new ClientConnection(newClient);
                clients.add(clientHandler);
                System.out.println("New client connected. Total clients: " + clients.size());
            } catch (SocketTimeoutException e) {
                LOG.info("Server SocketTimeoutException : {}", e.getMessage());
            } catch (IOException e) {
                if (running) {
                    LOG.error("Error accepting connection: {}", e.getMessage());
                }
            }

            Iterator<ClientConnection> iterator = clients.iterator();
            while (iterator.hasNext()) {
                ClientConnection client = iterator.next();
                client.socket.setSoTimeout(5);

                try {
                    int available = client.inputStream.available();


                    if (available > 0) {
                        byte[] buffer = new byte[Math.min(available, 4096)];
                        int bytesRead = client.inputStream.read(buffer);

                        if (bytesRead == -1) {
                            System.out.println("Client disconnected");
                            client.close();
                            iterator.remove();
                            continue;
                        }

                        String part = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                        client.messageBuilder.append(part);

                        int newlineIndex;
                        while ((newlineIndex = client.messageBuilder.indexOf("\n")) != -1) {
                            String message = client.messageBuilder.substring(0, newlineIndex).trim();
                            client.messageBuilder.delete(0, newlineIndex + 1);

                            System.out.println("Client's message: " + message);

                            if ("exit".equalsIgnoreCase(message)) {
                                client.outputStream.write("Goodbye!\n".getBytes(StandardCharsets.UTF_8));
                                client.outputStream.flush();
                                client.close();
                                iterator.remove();
                                System.out.println("Client disconnected via exit command");
                                break;
                            }

                            String response = "Server's " + message + "\n";
                            client.outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                            client.outputStream.flush();
                        }
                    }
                } catch (IOException e) {
                    LOG.error("Client connection error: {}", e.getMessage());
                    client.close();
                    iterator.remove();
                }
            }
        }

        for (ClientConnection client : clients) {
            client.close();
        }
        clients.clear();

        System.out.println("Server has stopped.");
    }

    @Override
    public void stop() throws IOException {
        System.out.println("Stopping server...");
        running = false;
        for (ClientConnection client : clients) {
            client.close();
        }
        clients.clear();

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }

        System.out.println("Server completely stopped.");
    }

    public static void main(String[] args) {
        int port = 5001;
        SingleThreadMultiClientEchoServer server = new SingleThreadMultiClientEchoServer();

        Thread serverThread = new Thread(() -> {
            try {
                server.start(port);
            } catch (IOException e) {
                System.out.println("Server error: " + e.getMessage());
            }
        });
        serverThread.start();

    }
}