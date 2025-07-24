package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolFTP {

    private static final int COMMAND_PORT = 5001;
    private static final int DATA_PORT = 5002;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(ThreadPoolFTP.class);

    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    private void start() throws IOException {
        try (
                ServerSocket commandSocketServer = new ServerSocket(COMMAND_PORT)
        ) {
            LOG.info("Server listening on COMMAND_PORT={} and DATA_PORT={}", COMMAND_PORT, DATA_PORT);

            while (true) {
                LOG.info("Waiting for command connection...");
                Socket commandSocket = commandSocketServer.accept();
                LOG.info("Command connection established: {}", commandSocket.getInetAddress());


                pool.submit(new FTPHandler(commandSocket));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        ThreadPoolFTP server = new ThreadPoolFTP();
        server.start();
    }
}
