package org.example.connection;

import org.example.parser.FTPCommandProcessor;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

public class ClientSession {
    private final SocketChannel channel;
    private ByteBuffer readBuffer;
    private final Queue<ByteBuffer> writeQueue;
    private final FTPCommandProcessor processor;

    public ClientSession(SocketChannel channel, FTPCommandProcessor processor) {
        this.channel = channel;
        this.processor = processor;
        this.readBuffer = ByteBuffer.allocate(8192);
        this.writeQueue = new ArrayDeque<>();
    }

    public SocketChannel getChannel() { return channel; }
    public ByteBuffer getReadBuffer() { return readBuffer; }
    public Queue<ByteBuffer> getWriteQueue() { return writeQueue; }
    public FTPCommandProcessor getProcessor() { return processor; }

    public void expandBuffer(int minCapacity) {
        int newCapacity = Math.min(readBuffer.capacity() * 2, 65536);
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        readBuffer.flip();
        newBuffer.put(readBuffer);
        readBuffer = newBuffer;
    }
}