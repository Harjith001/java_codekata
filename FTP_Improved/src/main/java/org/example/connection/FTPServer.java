package org.example.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.example.command.FTPCommandExecutor;
import org.example.parser.FTPCommandProcessor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer {
    private final int port;
    private static final Logger LOG = (Logger) LogManager.getLogger(FTPServer.class);

    public FTPServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("FTP Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected");

                handleClient(clientSocket);
            }
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            FTPCommandExecutor executor = new FTPCommandExecutor();
            FTPCommandProcessor processor = new FTPCommandProcessor(executor);

            String line;
            while ((line = in.readLine()) != null) {
                String response = processor.process(line);

                if (response.equals("QUIT")) {
                    break;
                }
                int length = response.length();

                out.write("Content-Length : "+ length);
                out.newLine();

                out.write(response);
                out.newLine();
                out.flush();
            }

            System.out.println("Client disconnected.");
        } catch (IOException e) {
            LOG.error("Client connection IO Exception{}", e.getMessage());
        }
    }

}
