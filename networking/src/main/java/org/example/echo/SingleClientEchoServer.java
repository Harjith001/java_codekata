package org.example.echo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SingleClientEchoServer implements EchoServer {

    private ServerSocket serverSocket;
    private volatile boolean running = true;

    @Override
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server listening at port "+ port);


        Socket connectionSocket = serverSocket.accept();

        while(running) {

            try (
                    InputStream inputStream = connectionSocket.getInputStream();
                    OutputStream outputStream = connectionSocket.getOutputStream()
            ) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer);
                    outputStream.flush();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 5001;
        SingleClientEchoServer server = new SingleClientEchoServer();
        server.start(port);
    }
}
