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
    private final ConcurrentHashMap<SocketChannel, ByteBuffer> pendingWrites = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<SocketChannel, ByteBuffer> readBuffers = new ConcurrentHashMap<>();


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
        ByteBuffer buffer = readBuffers.computeIfAbsent(client, ch -> ByteBuffer.allocate(8192)); // or use dynamic buffer

        ByteBuffer temp = ByteBuffer.allocate(4096);
        int bytesRead = client.read(temp);

        if (bytesRead <= 0) {
            closeConnection(key);
            return;
        }

        temp.flip();
        if (buffer.remaining() < temp.remaining()) {
            ByteBuffer expanded = ByteBuffer.allocate(buffer.capacity() + temp.remaining());
            buffer.flip();
            expanded.put(buffer);
            buffer = expanded;
            readBuffers.put(client, buffer);
        }

        buffer.put(temp);
        buffer.flip();
        if (!writeData(client, buffer)) {
            ByteBuffer remaining = ByteBuffer.allocate(buffer.remaining());
            remaining.put(buffer);
            remaining.flip();
            pendingWrites.put(client, remaining);
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        }
        buffer.clear();
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer pendingData = pendingWrites.get(client);

        if (pendingData != null) {
            if (writeData(client, pendingData)) {
                pendingWrites.remove(client);
                key.interestOps(SelectionKey.OP_READ);
            }
        } else {
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

            pendingWrites.remove(client);

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
        new Thread(() -> {
            try {
                server.stop();
            } catch (IOException e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }).start();

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