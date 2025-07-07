package org.example.echo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolEchoServer implements EchoServer {
    private static final int THREAD_COUNT = 10;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = true;

    @Override
    public void start(int port) throws IOException {
        threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        serverSocket = new ServerSocket(port);
        System.out.println("ThreadPoolEchoServer listening at port " + port);

        while (running) {
            try {
                Socket connectionSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(connectionSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket socket) {
        try (Socket clientSocket = socket;
             DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream())) {

            String messageReceived = dataIn.readUTF();
            System.out.println("Client's message: " + messageReceived);
            dataOut.writeUTF("Server's " + messageReceived);

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }
    public static void main(String[] args) {
        int port = 5001;
        ThreadPoolEchoServer server = new ThreadPoolEchoServer();

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
