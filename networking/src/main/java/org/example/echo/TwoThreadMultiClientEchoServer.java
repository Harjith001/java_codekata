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

    private final LinkedBlockingQueue<ClientConnection> newClientQueue = new LinkedBlockingQueue<>();

    private Thread acceptorThread;
    private Thread clientHandlerThread;

    @Override
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server listening at port " + port);

        acceptorThread = new Thread(() -> {
            while (running) {
                try {
                    Socket newClient = serverSocket.accept();
                    ClientConnection clientHandler = new ClientConnection(newClient);
                    newClientQueue.offer(clientHandler);
                    System.out.println("New client connected and added to the queue");
                } catch (SocketTimeoutException e) {
                    // Timeout is expected, continue loop
                } catch (IOException e) {
                    if (running) {
                        LOG.error("Error accepting connection: {}", e.getMessage());
                    }
                }
            }
        });

        clientHandlerThread = new Thread(new ClientHandler(newClientQueue));

        acceptorThread.start();
        clientHandlerThread.start();

        try {
            acceptorThread.join();
            clientHandlerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (ClientConnection client : newClientQueue) {
            client.close();
        }
        newClientQueue.clear();
        System.out.println("Server has stopped.");
    }

    @Override
    public void stop() throws IOException {
        System.out.println("Stopping server...");
        running = false;

        if (acceptorThread != null) {
            acceptorThread.interrupt();
        }
        if (clientHandlerThread != null) {
            clientHandlerThread.interrupt();
        }

        for (ClientConnection client : newClientQueue) {
            client.close();
        }
        newClientQueue.clear();

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