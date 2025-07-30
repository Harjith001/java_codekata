package org.example.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.example.parser.FTPCommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Queue;

import static org.example.command.FTPCommandExecutor.SERVER_DIR;

public class ClientSession {
    private final SocketChannel channel;
    private ByteBuffer readBuffer;
    private final Queue<ByteBuffer> writeQueue;
    private final FTPCommandProcessor processor;
    private static final Logger LOG = (Logger) LogManager.getLogger(ClientSession.class);

    private boolean isReceivingFile = false;
    private String uploadFilename;
    private int expectedFileSize;
    private int receivedFileBytes = 0;
    private ByteArrayOutputStream fileDataStream;

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


    public boolean isReceivingFile() { return isReceivingFile; }
    public String getUploadFilename() { return uploadFilename; }
    public int getExpectedFileSize() { return expectedFileSize; }
    public int getReceivedFileBytes() { return receivedFileBytes; }

    private FileChannel fileChannel;
    private Path uploadPath;

    public void startFileUpload(String filename, int fileSize) throws IOException {
        if (fileSize > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum allowed size");
        }

        this.uploadFilename = filename;
        this.expectedFileSize = fileSize;
        this.receivedFileBytes = 0;
        this.isReceivingFile = true;

        // Create the file immediately and open a channel to it
        Path dirPath = Paths.get(SERVER_DIR);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        this.uploadPath = dirPath.resolve(filename);
        this.fileChannel = FileChannel.open(
                uploadPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public void appendFileData(byte[] data) throws IOException {
        fileChannel.write(ByteBuffer.wrap(data));
        receivedFileBytes += data.length;
    }

    public String completeFileUpload() throws IOException {
        try {
            fileChannel.close();
            return "226 File uploaded successfully: " + uploadFilename +
                    " (" + receivedFileBytes + " bytes)";
        } finally {
            resetUploadState();
        }
    }

    private void resetUploadState() {
        isReceivingFile = false;
        uploadFilename = null;
        expectedFileSize = 0;
        receivedFileBytes = 0;
        try {
            if (fileChannel != null) {
                fileChannel.close();
            }
        } catch (IOException e) {
            LOG.error("Error closing file channel", e);
        }
        fileChannel = null;
        uploadPath = null;
    }

}
