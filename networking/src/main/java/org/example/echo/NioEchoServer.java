package org.example.echo;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class NioEchoServer implements EchoServer {

    private Selector selector;
    private ServerSocketChannel serverSocket;
    private volatile boolean running;
    private Thread serverThread;

    @Override
    public void start(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        running = true;

        serverThread = new Thread(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(256);

            try {
                while (running) {
                    if (selector.select(100) == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel client = server.accept();
                            if (client != null) {
                                client.configureBlocking(false);
                                client.register(selector, SelectionKey.OP_READ);
                            }
                        }

                        if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            buffer.clear();
                            int read = client.read(buffer);

                            if (read == -1) {
                                client.close();
                                continue;
                            }

                            String msg = new String(buffer.array(), 0, read);
                            System.out.println("Received: " + msg);

                            String response = "Server's " + msg;
                            ByteBuffer outBuffer = ByteBuffer.wrap(response.getBytes());
                            while (outBuffer.hasRemaining()) {
                                client.write(outBuffer);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            } finally {
                try {
                    stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        serverThread.start();
        System.out.println("Server started on port " + port);
    }

    @Override
    public void stop() throws IOException {
        running = false;
        if (selector != null) {
            selector.wakeup();
        }

        if (serverThread != null) {
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (serverSocket != null) {
            serverSocket.close();
        }
        if (selector != null) {
            selector.close();
        }
        System.out.println("Server stopped");
    }
}
