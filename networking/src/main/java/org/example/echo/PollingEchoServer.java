package org.example.echo;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PollingEchoServer implements EchoServer {

    private ServerSocket serverSocket;
    private final List<ClientConnection> clients = new ArrayList<>();
    private volatile boolean running = true;

    @Override
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Polling Echo Server listening at port " + port);

        new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(100);
                    clients.add(new ClientConnection(clientSocket));
                    System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
                } catch (IOException ignored) {}
            }
        }).start();

        while (running) {
            Iterator<ClientConnection> iterator = clients.iterator();
            while (iterator.hasNext()) {
                ClientConnection client = iterator.next();
                try {
                    if (client.in.available() > 0) {
                        String line = client.readLine();
                        if (line == null) {
                            client.close();
                            iterator.remove();
                            continue;
                        }
                        System.out.println("Received: " + line);

                        if ("exit".equalsIgnoreCase(line.trim())) {
                            client.send("Goodbye!\n");
                            client.close();
                            iterator.remove();
                        } else {
                            client.send("Server's " + line + "\n");
                        }
                    }
                } catch (IOException e) {
                    client.close();
                    iterator.remove();
                }
            }

        }

        stop();
    }

    @Override
    public void stop() throws IOException {
        running = false;
        for (ClientConnection client : clients) {
            client.close();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        System.out.println("Server stopped.");
    }

    // Inner class for client connection
    static class ClientConnection {
        Socket socket;
        InputStream in;
        OutputStream out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        ClientConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
        }

        String readLine() throws IOException {
            while (in.available() > 0) {
                int b = in.read();
                if (b == -1) return null;
                if (b == '\n') {
                    String line = buffer.toString(StandardCharsets.UTF_8).trim();
                    buffer.reset();
                    return line;
                } else {
                    buffer.write(b);
                }
            }
            return null;
        }

        void send(String message) throws IOException {
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        void close() {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 5001;
        PollingEchoServer server = new PollingEchoServer();
        server.start(port);
    }
}
