package org.example.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.example.command.FTPCommandExecutor;
import org.example.parser.FTPCommandProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class NIOServer {
    private static final Logger LOG = (Logger) LogManager.getLogger(NIOServer.class);
    private static final int PORT = 2121;
    private static final int WRITE_QUEUE_LIMIT = 10;

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private boolean running = false;
    private final Map<SocketChannel, ClientSession> clientSessions = new HashMap<>();

    public void start(int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running = true;
        LOG.info("NIO FTP Server started on port {}", port);

        while (running) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }

                try {
                    if (key.isAcceptable()) {
                        handleAccept();
                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (IOException e) {
                    LOG.error("Error handling connection", e);
                    closeConnection(key);
                }
            }
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel client = serverChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);

        FTPCommandExecutor executor = new FTPCommandExecutor();
        FTPCommandProcessor processor = new FTPCommandProcessor(executor);
        ClientSession session = new ClientSession(client, processor);
        clientSessions.put(client, session);

        LOG.info("New client connected: {}", client.getRemoteAddress());

        // Send welcome message
        String welcome = "220 FTP Server Ready\r\n";
        session.getWriteQueue().add(ByteBuffer.wrap(welcome.getBytes()));
        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientSession session = clientSessions.get(client);

        int bytesRead = client.read(session.getReadBuffer());

        if (bytesRead == -1) {
            closeConnection(key);
            return;
        }

        if (session.isReceivingFile()) {
            handleFileUpload(session, key);
            return;
        }

        ByteBuffer buffer = session.getReadBuffer();
        buffer.flip();


        while (buffer.hasRemaining()) {
            int newlinePos = findNewline(buffer);
            if (newlinePos == -1) {
                if (buffer.remaining() >= session.getMaxCommandSize()) {
                    LOG.warn("Max command size reached without newline from {}", client.getRemoteAddress());
                    closeConnection(key);
                    return;
                }
                if (buffer.remaining() > buffer.capacity() - buffer.position()) {
                    session.expandCommandBuffer(buffer.position() + buffer.remaining());
                    buffer = session.getReadBuffer();
                }
                buffer.compact();
                return;
            }

            byte[] lineBytes = new byte[newlinePos];
            buffer.get(lineBytes);
            buffer.get();

            String command = new String(lineBytes).trim();
            processCommand(session, key, command);
        }
        buffer.compact();
    }

    private void handleFileUpload(ClientSession session, SelectionKey key) throws IOException {
        ByteBuffer buffer = session.getReadBuffer();
        int bytesAvailable = buffer.position();
        int bytesNeeded = session.getExpectedFileSize() - session.getReceivedFileBytes();
        int bytesToRead = Math.min(bytesAvailable, bytesNeeded);

        buffer.flip();
        buffer.limit(buffer.position() + bytesToRead);

        byte[] chunk = new byte[bytesToRead];
        buffer.get(chunk);
        session.appendFileData(chunk);

        buffer.compact();

        if (session.getReceivedFileBytes() >= session.getExpectedFileSize()) {
            String response = session.completeFileUpload();
            String fullResponse = response + "\r\n";
            session.getWriteQueue().add(ByteBuffer.wrap(fullResponse.getBytes()));
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }
    private void processCommand(ClientSession session, SelectionKey key, String command) {
        try {
            String response = session.getProcessor().process(command, session);

            if ("abort".equals(response)) {
                closeConnection(key);
                return;
            }

            if ("file_upload_ready".equals(response)) {
                String ackResponse = "150 Ready to receive file data\r\n";
                session.getWriteQueue().add(ByteBuffer.wrap(ackResponse.getBytes()));
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                return;
            }

            String fullResponse = response + "\r\n";
            Queue<ByteBuffer> queue = session.getWriteQueue();

            if (queue.size() >= WRITE_QUEUE_LIMIT) {
                LOG.warn("Write queue limit reached for {}", session.getChannel().getRemoteAddress());
                closeConnection(key);
                return;
            }

            queue.add(ByteBuffer.wrap(fullResponse.getBytes()));
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (Exception e) {
            LOG.error("Error processing command", e);
            closeConnection(key);
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientSession session = clientSessions.get(client);
        if (session == null) {
            return;
        }

        Queue<ByteBuffer> queue = session.getWriteQueue();
        while (!queue.isEmpty()) {
            ByteBuffer buffer = queue.peek();
            client.write(buffer);

            if (buffer.hasRemaining()) {
                return;
            }

            queue.remove();
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    private int findNewline(ByteBuffer buffer) {
        for (int i = buffer.position(); i < buffer.limit(); i++) {
            if (buffer.get(i) == '\n') {
                if (i > buffer.position() && buffer.get(i - 1) == '\r') {
                    return (i - buffer.position()) - 1;
                }
                return i - buffer.position();
            }
        }
        return -1;
    }

    private void closeConnection(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        clientSessions.remove(client);
        try {
            LOG.info("Closing connection: {}", client.getRemoteAddress());
            client.close();
        } catch (IOException e) {
            LOG.error("Error closing connection", e);
        }
        key.cancel();
    }

    public void stop() throws IOException {
        running = false;
        if (selector != null) {
            selector.wakeup();
            for (SelectionKey key : selector.keys()) {
                if (key.channel() instanceof SocketChannel) {
                    closeConnection(key);
                }
            }
            selector.close();
        }
        if (serverChannel != null) {
            serverChannel.close();
        }
        LOG.info("FTP Server stopped");
    }

    public static void main(String[] args) {
        NIOServer server = new NIOServer();
        try {
            server.start(PORT);
        } catch (IOException e) {
            LOG.error("Server error", e);
        } finally {
            try {
                server.stop();
            } catch (IOException e) {
                LOG.error("Error stopping server", e);
            }
        }
    }
}
