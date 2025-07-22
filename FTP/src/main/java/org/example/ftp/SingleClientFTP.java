package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleClientFTP {
    private static final int COMMAND_PORT = 5001;
    private static final int DATA_PORT = 5002;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(SingleClientFTP.class);

    public void start() {
        try (
                ServerSocket commandServerSocket = new ServerSocket(COMMAND_PORT);
        ) {
            LOG.info("Server is listening on COMMAND_PORT={} and DATA_PORT={}", COMMAND_PORT, DATA_PORT);

            while (true) {
                try {
                    LOG.info("Waiting for command connection...");
                    Socket commandSocket = commandServerSocket.accept();
                    LOG.info("Command connection from: {}", commandSocket.getInetAddress());


                    FTPHandler handler = new FTPHandler(commandSocket);
                    handler.run();

                    commandSocket.close();
                    LOG.info("Client session ended.");
                } catch (IOException e) {
                    LOG.error("Error during client handling: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.error("Server start failed: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        new SingleClientFTP().start();
    }
}
