package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiClientFTP {
    private static final int COMMAND_PORT = 5001;
    private static final int DATA_PORT = 5002;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(MultiClientFTP.class);

    private void start() throws IOException {
        try (
                ServerSocket commandServerSocket = new ServerSocket(COMMAND_PORT)
        ) {
            LOG.info("FTP Server started on ports: COMMAND={}, DATA={}", COMMAND_PORT, DATA_PORT);

            while (true) {
                LOG.info("Waiting for client on command port...");
                Socket commandSocket = commandServerSocket.accept();
                LOG.info("Client connected on command port: {}", commandSocket.getInetAddress());


                new Thread(new FTPHandler(commandSocket)).start();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        new MultiClientFTP().start();
    }
}
