package file.copier;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class BufferedFileCopierTest {
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
        Files.write(sourceFile, "Sample content for testing.\n".repeat(100_000).getBytes());
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
    void copy() throws IOException {
        int bufferSize = 4096;
        BufferedFileCopier bf = new BufferedFileCopier(bufferSize);
        long time = bf.copy(sourceFile.toString(), destFile.toString());
        System.out.println("Copy time (nanoseconds): " + time);

        // ✅ Step 1: Check if destination file exists
        assertTrue(Files.exists(destFile), "Destination file should exist after copying.");

        // ✅ Step 2: Read both files
        byte[] sourceBytes = Files.readAllBytes(sourceFile);
        byte[] destBytes = Files.readAllBytes(destFile);

        // ✅ Step 3: Compare contents
        assertArrayEquals(sourceBytes, destBytes, "Files should be identical after copy.");
    }

}