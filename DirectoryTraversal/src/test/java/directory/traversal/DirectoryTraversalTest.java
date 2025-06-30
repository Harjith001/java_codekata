package directory.traversal;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryTraversalTest {

    private DirectoryTraversal traversal;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        traversal = new DirectoryTraversal();
        tempDir = Files.createTempDirectory("testDir");
    }

    @AfterEach
    void tearDown() {
        deleteRecursively(tempDir.toFile());
    }

    @Test
    void testListFilesWithExtension() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.txt"));
        Path file3 = Files.createFile(tempDir.resolve("file3.TXT")); // uppercase extension

        try (Stream<Path> resultStream = traversal.listFiles(tempDir, ".txt")) {
            List<Path> result = resultStream.map(Path::toAbsolutePath).toList();

            assertEquals(2, result.size());
            assertTrue(result.contains(file1.toAbsolutePath()));
            assertTrue(result.contains(file3.toAbsolutePath()));
        }
    }

    @Test
    void testFindContentInFiles() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.txt"));
        Path file2 = Files.createFile(tempDir.resolve("file2.txt"));
        Files.writeString(file1, "This file contains the word hello");
        Files.writeString(file2, "This one does not");

        try (Stream<Path> resultStream = traversal.findContent(tempDir, "hello")) {
            List<Path> result = resultStream.toList();

            assertEquals(1, result.size());
            assertEquals("file1.txt", result.getFirst().getFileName().toString());
        }
    }

    @Test
    void testNoFilesFound() {
        try (Stream<Path> resultStream = traversal.listFiles(tempDir, ".java")) {
            assertTrue(resultStream.findAny().isEmpty());
        }
    }

    @Test
    void testNoContentMatches() throws IOException {
        Files.createFile(tempDir.resolve("empty.txt"));

        try (Stream<Path> resultStream = traversal.findContent(tempDir, "notfound")) {
            assertTrue(resultStream.findAny().isEmpty());
        }
    }

    @Test
    void testNestedDirectorySearch() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("sub"));
        Path fileInSub = Files.createFile(subDir.resolve("note.txt"));
        Files.writeString(fileInSub, "Nested file with keyword test");

        try (Stream<Path> resultStream = traversal.findContent(tempDir, "test")) {
            List<Path> result = resultStream.toList();

            assertEquals(1, result.size());
            assertEquals("note.txt", result.getFirst().getFileName().toString());
        }
    }

    // Helper method to delete directories after test
    private void deleteRecursively(File file) {
        File[] allContents = file.listFiles();
        if (allContents != null) {
            for (File f : allContents) {
                deleteRecursively(f);
            }
        }
        file.delete();
    }
}
