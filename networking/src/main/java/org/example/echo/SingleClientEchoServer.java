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
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connectionSocket.getInputStream(), StandardCharsets.UTF_8));
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(connectionSocket.getOutputStream(), StandardCharsets.UTF_8))
            ) {

                String messageReceived;
                while ((messageReceived = reader.readLine()) != null) {
                    System.out.println("Client's message: " + messageReceived);

                    if ("exit".equalsIgnoreCase(messageReceived.trim())) {
                        writer.write("Goodbye!\n");
                        writer.flush();
                        break;
                    }
                    writer.write("Server's " + messageReceived + "\n");
                    writer.flush();
                }
                System.out.println("Client disconnected");
            } catch (IOException e) {
                //log
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
