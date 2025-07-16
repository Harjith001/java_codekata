package org.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadedNioEchoServer implements EchoServer {
    private static final int PORT = 5001;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService workerPool;
    private final static short MAX_CLIENTS_PER_WORKER = 10;

    @Override
    public void start(int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Use a fixed thread pool with number of available processors
        workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        running.set(true);
        System.out.println("Server started on port " + port);

        while (running.get()) {
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                try {
                    if (key.isAcceptable()) {
                        acceptClient(key);
                    } else if (key.isReadable()) {
                        //key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                        workerPool.submit(() -> {
                            try {
                                handleClient(key);
                            } catch (IOException e) {
                                closeClient(key);
                            }
                        });
                    }
                } catch (CancelledKeyException e) {
                    closeClient(key);
                }
            }
        }

        shutdown();
    }

    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        if (client != null) {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("Accepted new connection: " + client.getRemoteAddress());
        }
    }

    private void handleClient(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int bytesRead = client.read(buffer);

        if (bytesRead <= 0) {
            closeClient(key);
            return;
        }

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        String message = new String(data, StandardCharsets.UTF_8).trim();

        String response = "Server's " + message + "\n";
        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
        client.write(responseBuffer);

        key.selector().wakeup();
    }

    private void closeClient(SelectionKey key) {
        try {
            key.cancel();
            key.channel().close();
            System.out.println("Closed connection: " + key.channel());
        } catch (IOException ignored) {}
    }

    private void shutdown() throws IOException {
        if (selector != null) selector.close();
        if (serverChannel != null) serverChannel.close();
        if (workerPool != null) workerPool.shutdown();
        System.out.println("Server stopped.");
    }

    @Override
    public void stop() throws IOException {
        running.set(false);
        if (selector != null) selector.wakeup();
    }

    public static void main(String[] args) throws IOException {
        NioEchoServer server = new NioEchoServer();
        new Thread(() -> {
            try {
                server.start(PORT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
