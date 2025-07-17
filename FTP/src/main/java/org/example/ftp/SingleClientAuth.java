package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SingleClientAuth extends FTPImplementation {

    private static final int PORT = 5001;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(SingleClientAuth.class);

    private final Queue<Socket> clients = new ConcurrentLinkedQueue<>();
    private volatile boolean clientConnected = false;

    private void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOG.info("Server is listening on PORT {}", PORT);

            // Start a background thread to process clients from the queue
            new Thread(this::processClientsFromQueue).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.getOutputStream().write("Connected to server. \n".getBytes());
                clients.add(clientSocket);
            }
        }
    }

    private void processClientsFromQueue() {
        while (true) {
            if (!clientConnected && !clients.isEmpty()) {
                Socket client = clients.poll();
                if (client != null) {
                    clientConnected = true;
                    LOG.info("Serving client: {}", client.getInetAddress());

                    new Thread(() -> {
                        try {
                            handleClient(client);
                        } finally {
                            try {
                                client.close();
                            } catch (IOException e) {
                                LOG.error("Error closing client: {}", e.getMessage());
                            }
                            clientConnected = false;
                            LOG.info("Client disconnected. Ready for next in queue.");
                        }
                    }).start();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        SingleClientAuth server = new SingleClientAuth();
        server.start();
    }
}
