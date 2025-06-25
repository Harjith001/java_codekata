package file.copier;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

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
    void copy() throws IOException {
        FileStreamCopier fs = new FileStreamCopier(4096);
        long time = fs.copy(sourceFile.toString(), destFile.toString());
        System.out.print(time);
    }
}