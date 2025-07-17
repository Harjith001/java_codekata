package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleClientFTP extends FTPImplementation{
    private static final int PORT = 5001;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(SingleClientFTP.class);


    public void start() throws IOException {
        try( ServerSocket serverSocket = new ServerSocket(PORT)){
            LOG.info("Server is listening at PORT 80");

            while (true) {
                try{
                    Socket clientSocket = serverSocket.accept();
                    LOG.info("Client is connected to the server");

                    handleClient(clientSocket);
                } catch (IOException e) {
                    LOG.error("Client connection error : {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.error("Server start error : {}", e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(DIR);
        if(!dir.exists()) {
            dir.mkdir();
        }

        SingleClientFTP server = new SingleClientFTP();
        server.start();
    }
}
