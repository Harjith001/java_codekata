package org.example.echo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TwoThreadMultiClientEchoServer implements EchoServer {

    private static final Logger LOG = LogManager.getLogger(TwoThreadMultiClientEchoServer.class);

    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private final List<ClientConnection> clients = new ArrayList<>();
    private final BlockingQueue<ClientConnection> newClientQueue = new LinkedBlockingQueue<>();

    private Thread acceptorThread;
    private Thread clientHandlerThread;

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
        System.out.println("Server listening at port " + port);

        // Thread 1: Accept new client connections
        acceptorThread = new Thread(() -> {
            while (running) {
                try {
                    Socket newClient = serverSocket.accept();
                    ClientConnection clientHandler = new ClientConnection(newClient);
                    newClientQueue.offer(clientHandler);
                    System.out.println("New client connected and queued");
                } catch (SocketTimeoutException e) {
                    // Timeout is expected, continue loop
                } catch (IOException e) {
                    if (running) {
                        LOG.error("Error accepting connection: {}", e.getMessage());
                    }
                }
            }
        }, "ClientAcceptor");

        clientHandlerThread = new Thread(() -> {
            while (running) {
                try {
                    // Add any new clients from the queue
                    ClientConnection newClient;
                    while ((newClient = newClientQueue.poll()) != null) {
                        clients.add(newClient);
                        System.out.println("New client added to handler. Total clients: " + clients.size());
                    }

                    // Handle existing clients
                    Iterator<ClientConnection> iterator = clients.iterator();
                    while (iterator.hasNext()) {
                        ClientConnection client = iterator.next();

                        try {
                            client.socket.setSoTimeout(5);
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

                    // Small sleep to prevent busy waiting
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "ClientHandler");

        // Set socket timeout for non-blocking accept
        serverSocket.setSoTimeout(50);

        // Start both threads
        acceptorThread.start();
        clientHandlerThread.start();

        // Wait for both threads to complete
        try {
            acceptorThread.join();
            clientHandlerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Clean up remaining clients
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

        // Interrupt threads if they're still running
        if (acceptorThread != null) {
            acceptorThread.interrupt();
        }
        if (clientHandlerThread != null) {
            clientHandlerThread.interrupt();
        }

        // Close all client connections
        for (ClientConnection client : clients) {
            client.close();
        }
        clients.clear();

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }

        System.out.println("Server completely stopped.");
    }

    public static void main(String[] args) {
        int port = 5001;
        TwoThreadMultiClientEchoServer server = new TwoThreadMultiClientEchoServer();

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