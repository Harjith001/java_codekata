package org.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class NioEchoServer implements EchoServer {
    private static final int PORT = 5001;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private boolean running = false;

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
                if (key.isAcceptable()) {
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    handleClient((SocketChannel) key.channel());
                }
            }
        }
    }

    private void handleClient(SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int bytesRead = client.read(buffer);

        if (bytesRead <= 0) {
            client.close();
            return;
        }
        buffer.flip();
        client.write(buffer);
    }

    @Override
    public void stop() throws IOException {
        running = false;
        if (serverChannel != null) serverChannel.close();
        if (selector != null){
            selector.wakeup();
            selector.close();
        }

    }

    public static void main(String[] args) throws IOException {
        NioEchoServer server = new NioEchoServer();
        new Thread(()->{
            try {
                server.start(PORT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }
}