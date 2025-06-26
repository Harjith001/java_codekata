package file.copier;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStreamCopierTest {

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
    void copyWithDifferentSizes() throws IOException {
        int size = 4096;

        FileStreamCopier fs1 = new FileStreamCopier(size);

        long time = fs1.copyWithoutBuffer(sourceFile.toString(), destFile.toString());
        System.out.println("File stream without byte array size : " + time);

        assertTrue(Files.exists(destFile), "Destination file should exist after copyWithoutBuffer");

        byte[] sourceBytes = Files.readAllBytes(sourceFile);
        byte[] destBytes = Files.readAllBytes(destFile);
        assertArrayEquals(sourceBytes, destBytes, "Files should be equal after copyWithoutBuffer");

        Files.delete(destFile);

        time = fs1.copy(sourceFile.toString(), destFile.toString());
        System.out.println("File stream with byte array size 4096 : " + time);

        assertTrue(Files.exists(destFile), "Destination file should exist after copy");

        destBytes = Files.readAllBytes(destFile);
        assertArrayEquals(sourceBytes, destBytes, "Files should be equal after buffered copy");
    }
}