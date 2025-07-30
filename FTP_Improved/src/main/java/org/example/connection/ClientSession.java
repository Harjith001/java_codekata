package org.example.connection;

import org.example.parser.FTPCommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

public class ClientSession {
    private final SocketChannel channel;
    private ByteBuffer readBuffer;
    private final Queue<ByteBuffer> writeQueue;
    private final FTPCommandProcessor processor;

    // File upload state
    private boolean isReceivingFile = false;
    private String uploadFilename;
    private int expectedFileSize;
    private int receivedFileBytes = 0;
    private ByteArrayOutputStream fileDataStream;

    // Buffer limits
    private static final int MAX_COMMAND_SIZE = 8192;
    private static final int MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

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

    public void expandCommandBuffer(int minCapacity) {
        int newCapacity = Math.min(readBuffer.capacity() * 2, MAX_COMMAND_SIZE);
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        readBuffer.flip();
        newBuffer.put(readBuffer);
        readBuffer = newBuffer;
    }

    public void expandFileBuffer(int minCapacity) {
        int newCapacity = Math.min(readBuffer.capacity() * 2, MAX_FILE_SIZE);
        if (newCapacity < minCapacity && minCapacity <= MAX_FILE_SIZE) {
            newCapacity = minCapacity;
        }
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        readBuffer.flip();
        newBuffer.put(readBuffer);
        readBuffer = newBuffer;
    }

    public int getMaxCommandSize() { return MAX_COMMAND_SIZE; }
    public int getMaxFileSize() { return MAX_FILE_SIZE; }

    // File upload methods
    public void startFileUpload(String filename, int fileSize) throws IOException {
        if (fileSize > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum allowed size");
        }

        this.uploadFilename = filename;
        this.expectedFileSize = fileSize;
        this.receivedFileBytes = 0;
        this.isReceivingFile = true;
        this.fileDataStream = new ByteArrayOutputStream(fileSize);

        // Expand buffer if needed for large files
        if (fileSize > readBuffer.capacity()) {
            expandFileBuffer(Math.min(fileSize, 64 * 1024)); // Max 64KB buffer
        }
    }

    public void appendFileData(byte[] data) throws IOException {
        fileDataStream.write(data);
        receivedFileBytes += data.length;
    }

    public String completeFileUpload() throws IOException {
        if (!isReceivingFile) {
            return "Error: No file upload in progress";
        }

        try {
            byte[] fileData = fileDataStream.toByteArray();
            String result = processor.saveFile(uploadFilename, fileData);

            // Reset state
            isReceivingFile = false;
            uploadFilename = null;
            expectedFileSize = 0;
            receivedFileBytes = 0;
            fileDataStream = null;

            return result;
        } catch (Exception e) {
            // Reset state on error
            isReceivingFile = false;
            uploadFilename = null;
            expectedFileSize = 0;
            receivedFileBytes = 0;
            if (fileDataStream != null) {
                fileDataStream.close();
                fileDataStream = null;
            }
            throw e;
        }
    }

    public boolean isReceivingFile() { return isReceivingFile; }
    public String getUploadFilename() { return uploadFilename; }
    public int getExpectedFileSize() { return expectedFileSize; }
    public int getReceivedFileBytes() { return receivedFileBytes; }
}
