package org.example.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class NioEchoServer implements EchoServer {

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start(int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running.set(true);

        while (running.get()) {
            selector.select(1000);
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
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = client.read(buffer);

        if (bytesRead <= 0) {
            client.close();
            return;
        }

        buffer.flip();

        short length = buffer.getShort();
        byte[] data = new byte[length];
        buffer.get(data);
        String message = new String(data, StandardCharsets.UTF_8);

        String response = "Server's " + message;
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        ByteBuffer responseBuffer = ByteBuffer.allocate(2 + responseBytes.length);
        responseBuffer.putShort((short) responseBytes.length);
        responseBuffer.put(responseBytes);
        responseBuffer.flip();

        client.write(responseBuffer);
    }

    @Override
    public void stop() throws IOException {
        running.set(false);
        if (selector != null) selector.wakeup();
        if (serverChannel != null) serverChannel.close();
        if (selector != null) selector.close();
    }
}