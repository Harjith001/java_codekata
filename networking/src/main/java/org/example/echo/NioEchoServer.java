package org.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class NioEchoServer implements EchoServer {
    private static final int PORT = 5001;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private boolean running = false;

    // Store pending write data for each client
    private final ConcurrentHashMap<SocketChannel, ByteBuffer> pendingWrites = new ConcurrentHashMap<>();

    @Override
    public void start(int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running = true;

        while (running) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                try {
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (IOException e) {
                    closeConnection(key);
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel client = serverChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client connected: " + client.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int bytesRead = client.read(buffer);

        if (bytesRead <= 0) {
            closeConnection(key);
            return;
        }

        buffer.flip();

        // Try to write immediately
        if (!writeData(client, buffer)) {
            // If we couldn't write all data, store it and register for write interest
            ByteBuffer remainingData = ByteBuffer.allocate(buffer.remaining());
            remainingData.put(buffer);
            remainingData.flip();

            pendingWrites.put(client, remainingData);

            // Add WRITE interest to the selection key
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer pendingData = pendingWrites.get(client);

        if (pendingData != null) {
            if (writeData(client, pendingData)) {
                // All data written successfully
                pendingWrites.remove(client);

                // Remove WRITE interest, keep only READ
                key.interestOps(SelectionKey.OP_READ);
            }
            // If writeData returns false, we keep the WRITE interest and try again later
        } else {
            // No pending data, remove WRITE interest
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private boolean writeData(SocketChannel client, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int bytesWritten = client.write(buffer);

            if (bytesWritten == 0) {
                return false;
            }

            if (bytesWritten < 0) {
                throw new IOException("Connection closed by client");
            }
        }
        return true;
    }

    private void closeConnection(SelectionKey key) {
        try {
            SocketChannel client = (SocketChannel) key.channel();
            System.out.println("Closing connection: " + client.getRemoteAddress());

            // Clean up pending writes
            pendingWrites.remove(client);

            // Close the channel and cancel the key
            client.close();
            key.cancel();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws IOException {
        running = false;
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (selector != null) {
            selector.wakeup();
            selector.close();
        }
        pendingWrites.clear();
    }

    public static void main(String[] args) throws IOException {
        NioEchoServer server = new NioEchoServer();

        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (IOException e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));

        new Thread(() -> {
            try {
                server.start(PORT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        System.out.println("Echo server started on port " + PORT);
    }
}