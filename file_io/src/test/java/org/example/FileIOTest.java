package org.example;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class FileIOTest {

    private static Path tempDir;
    private Path sourceFile;
    private Path destFile;

    @BeforeAll
    static void setupAll() throws IOException {
        tempDir = Files.createTempDirectory("fileio_test");
    }

    @AfterAll
    static void teardownAll() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @BeforeEach
    void setup() throws IOException {
        sourceFile = Files.createTempFile(tempDir, "source", ".txt");
        destFile = tempDir.resolve("dest.txt");
        Files.write(sourceFile, "Sample content for testing.".getBytes());
    }

    @AfterEach
    void cleanup() throws IOException {
        if (Files.exists(destFile)) {
            Files.delete(destFile);
        }
        if (Files.exists(sourceFile)) {
            Files.delete(sourceFile);
        }
    }

    @Test
    void testCopyUnbuffered() throws IOException {
        long time = FileIO.copyUnbuffered(sourceFile.toString(), destFile.toString());
        assertTrue(time >= 0, "Time should be non-negative");
        assertTrue(Files.exists(destFile), "Destination file should exist");
        assertArrayEquals(Files.readAllBytes(sourceFile), Files.readAllBytes(destFile), "Copied file content must match source");
    }

    @Test
    void testCopyBuffered() throws IOException {
        int bufferSize = 4096;
        long time = FileIO.copyBuffered(sourceFile.toString(), destFile.toString(), bufferSize);
        assertTrue(time >= 0, "Time should be non-negative");
        assertTrue(Files.exists(destFile), "Destination file should exist");
        assertArrayEquals(Files.readAllBytes(sourceFile), Files.readAllBytes(destFile), "Copied file content must match source");
    }

    @Test
    void testCopyUsingNIO() throws IOException {
        long time = FileIO.copyUsingNIO(sourceFile.toString(), destFile.toString());
        assertTrue(time >= 0, "Time should be non-negative");
        assertTrue(Files.exists(destFile), "Destination file should exist");
        assertArrayEquals(Files.readAllBytes(sourceFile), Files.readAllBytes(destFile), "Copied file content must match source");
    }

}
