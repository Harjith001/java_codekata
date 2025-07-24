package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiClientFTP{

    private static final int PORT = 5001;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(MultiClientFTP.class);

    private void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOG.info("Server is listening on PORT {}", PORT);


            while (true) {
                Socket clientSocket = serverSocket.accept();
                //clientSocket.getOutputStream().write("Connected to server. \n".getBytes());
                FTPHandler handler = new FTPHandler(clientSocket);
                handler.run();

            }
        }
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        MultiClientFTP server = new MultiClientFTP();
        server.start();
    }
}
