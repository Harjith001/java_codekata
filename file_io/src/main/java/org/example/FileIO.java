package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FileIO {

    private static final Logger LOG = LogManager.getLogger(FileIO.class);
    private static final String SOURCE = "src/main/resources/largeFile.txt";
    private static final String DEST_UNBUFFERED = "copy_unbuffered.txt";
    private static final String DEST_BUFFERED = "copy_buffered.txt";
    private static final String DEST_NIO = "copy_nio.txt";

    public static void main(String[] args) throws IOException {
        LOG.info("=== File Copy Performance ===");

        long timeUnbuffered = copyUnbuffered(SOURCE, DEST_UNBUFFERED);
        LOG.info("Unbuffered copy time: {} ms", timeUnbuffered);

        long timeBuffered = copyBuffered(SOURCE, DEST_BUFFERED, 8192);
        LOG.info("Buffered copy time (8KB buffer): {} ms", timeBuffered);

        copyBuffered(SOURCE, "copy_buffered_4kb.txt", 4096);
        copyBuffered(SOURCE, "copy_buffered_32kb.txt", 32 * 1024);
        copyBuffered(SOURCE, "copy_buffered_64kb.txt", 64 * 1024);

        long timeNIO = copyUsingNIO(SOURCE, DEST_NIO);
        LOG.info("NIO copy time: {} ms", timeNIO);

     }

    public static long copyUnbuffered(String source, String dest) throws IOException {
        long start = System.currentTimeMillis();

        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {
            int data;
            while ((data = fis.read()) != -1) {
                fos.write(data);
            }
        }

        long end = System.currentTimeMillis();
        return end - start;
    }

    public static long copyBuffered(String source, String dest, int bufferSize) throws IOException {
        System.nanoTime();
        long start = System.currentTimeMillis();

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source), bufferSize);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest), bufferSize)) {

            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }
        long end = System.currentTimeMillis();
        LOG.info("Buffered copy with buffer size {}KB took: {} ms", bufferSize / 1024, end - start);
        return end - start;
    }

    public static long copyUsingNIO(String source, String dest) throws IOException {
        long start = System.currentTimeMillis();

        Path sourcePath = Paths.get(source);
        Path destPath = Paths.get(dest);
        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);

        long end = System.currentTimeMillis();
        return end - start;
    }
}
