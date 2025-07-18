package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolFTP extends FTPImplementation {

    private static final int PORT = 5001;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(ThreadPoolFTP.class);
    private final ExecutorService pool = Executors.newFixedThreadPool(3);


    private void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOG.info("Server is listening on PORT {}", PORT);


            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.getOutputStream().write("Connected to server. \n".getBytes());
                pool.submit(() -> {
                    handleClient(clientSocket);
                });
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
