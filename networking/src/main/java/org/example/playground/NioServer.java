package org.example.playground;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class NioServer {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(5001));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port 5001");

        ByteBuffer buffer = ByteBuffer.allocate(256);

        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isAcceptable()) {
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
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
                    client.write(outBuffer);
                }
            }
        }
    }
}
