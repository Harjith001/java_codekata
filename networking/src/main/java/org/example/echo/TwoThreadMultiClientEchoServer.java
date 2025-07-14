package org.example.echo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TwoThreadMultiClientEchoServer implements EchoServer {
    private static final short PORT = 5001;
    private static final Logger LOG = LogManager.getLogger(TwoThreadMultiClientEchoServer.class);

    private ServerSocket serverSocket;
    private volatile boolean running = true;

    //two queues

    private final List<ClientConnection> clients = new ArrayList<>();
    private final BlockingQueue<ClientConnection> newClientQueue = new LinkedBlockingQueue<>();
    

    private Thread acceptorThread;
    private Thread clientHandlerThread;

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
                            client.outputStream.write(buffer);
                        }
                    } catch (IOException e) {
                        LOG.error("Client connection error: {}", e.getMessage());
                        client.close();
                        iterator.remove();
                    }
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
        TwoThreadMultiClientEchoServer server = new TwoThreadMultiClientEchoServer();

        Thread serverThread = new Thread(() -> {
            try {
                server.start(PORT);
            } catch (IOException e) {
                System.out.println("Server error: " + e.getMessage());
            }
        });
        serverThread.start();
    }
}