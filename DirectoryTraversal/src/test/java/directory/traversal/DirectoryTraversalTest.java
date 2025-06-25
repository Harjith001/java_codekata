package directory.traversal;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

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
        Path file2 = Files.createFile(tempDir.resolve("file2.md"));
        Path file3 = Files.createFile(tempDir.resolve("file3.TXT")); // uppercase extension

        List<String> result = traversal.listFiles(tempDir.toString(), ".txt");

        assertEquals(2, result.size());
        assertTrue(result.contains(file1.toAbsolutePath().toString()));
        assertTrue(result.contains(file3.toAbsolutePath().toString()));
    }

    @Test
    void testFindContentInFiles() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.txt"));
        Path file2 = Files.createFile(tempDir.resolve("file2.txt"));
        Files.writeString(file1, "This file contains the word hello");
        Files.writeString(file2, "This one does not");

        List<String> result = traversal.findContent(tempDir.toString(), "hello");

        assertEquals(1, result.size());
        assertTrue(result.getFirst().contains("file1.txt"));
    }

    @Test
    void testNoFilesFound() {
        List<String> result = traversal.listFiles(tempDir.toString(), ".java");
        assertTrue(result.isEmpty());
    }

    @Test
    void testNoContentMatches() throws IOException {
        Files.createFile(tempDir.resolve("empty.txt"));
        List<String> result = traversal.findContent(tempDir.toString(), "notfound");
        assertTrue(result.isEmpty());
    }

    @Test
    void testNestedDirectorySearch() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("sub"));
        Path fileInSub = Files.createFile(subDir.resolve("note.txt"));
        Files.writeString(fileInSub, "Nested file with keyword test");

        List<String> result = traversal.findContent(tempDir.toString(), "test");

        assertEquals(1, result.size());
        assertTrue(result.getFirst().contains("note.txt"));
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
