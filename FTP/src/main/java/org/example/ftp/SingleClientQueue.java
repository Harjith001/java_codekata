package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class SingleClientQueue {
    private static final int COMMAND_PORT = 5001;
    private static final int DATA_PORT = 5002;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(SingleClientQueue.class);

    private final Queue<Socket> clientQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean clientConnected = false;

    private void start() throws IOException {
        try (
                ServerSocket commandServerSocket = new ServerSocket(COMMAND_PORT);
        ) {
            LOG.info("Server listening on COMMAND_PORT={}, DATA_PORT={}", COMMAND_PORT, DATA_PORT);

            new Thread(this::processClientQueue).start();

            while (true) {
                LOG.info("Waiting for new client...");

                Socket commandSocket = commandServerSocket.accept();
                LOG.info("Command socket connected from {}", commandSocket.getInetAddress());


                clientQueue.add(commandSocket);
            }
        }
    }

    private void processClientQueue() {
        while (true) {
            if (!clientConnected && !clientQueue.isEmpty()) {
                Socket client = clientQueue.poll();
                if (client != null) {
                    clientConnected = true;
                    LOG.info("Starting new client session");

                    new Thread(() -> {
                        try {
                            new FTPHandler(client).run();
                        } finally {
                            try {
                                client.close();
                            } catch (IOException e) {
                                LOG.error("Error closing sockets: {}", e.getMessage());
                            }
                            clientConnected = false;
                            LOG.info("Client session ended. Ready for next client.");
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

        new SingleClientQueue().start();
    }

    private static class ClientPair {
        Socket commandSocket;
        Socket dataSocket;

        public ClientPair(Socket commandSocket, Socket dataSocket) {
            this.commandSocket = commandSocket;
            this.dataSocket = dataSocket;
        }
    }
}
