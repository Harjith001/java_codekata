package org.example;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryTraversalTest {

    private static final Path TEST_DIR = Paths.get("temp_test_dir");

    @BeforeAll
    static void setup() throws IOException {

        Files.createDirectories(TEST_DIR);

        Files.write(TEST_DIR.resolve("file1.txt"), "This is Ben 10".getBytes());
        Files.write(TEST_DIR.resolve("file2.txt"), "Some random content".getBytes());

        Path subDir = Files.createDirectories(TEST_DIR.resolve("subdir"));
        Files.write(subDir.resolve("file3.txt"), "Ben 10 again!".getBytes());
    }

    @AfterAll
    static void teardown() throws IOException {
        Files.walk(TEST_DIR)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testSearchContent_FindsMatchingFiles() {
        DirectoryTraversal dt = new DirectoryTraversal();
        List<String> results = dt.search_content(TEST_DIR.toString(), "Ben 10");

        assertEquals(3, results.size());
        assertTrue(results.stream().anyMatch(path -> path.contains("file1.txt")));
        assertTrue(results.stream().anyMatch(path -> path.contains("file3.txt")));
    }

    @Test
    void testSearchContent_NoMatch() {
        DirectoryTraversal dt = new DirectoryTraversal();
        List<String> results = dt.search_content(TEST_DIR.toString(), "Nonexistent content");

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchInFile_Match() throws IOException {
        Path filePath = TEST_DIR.resolve("test_match.txt");
        Files.write(filePath, "Here is Ben 10!".getBytes());

        DirectoryTraversal dt = new DirectoryTraversal();
        boolean result = dt.search_in_file(filePath.toString(), "Ben 10");

        assertTrue(result);
    }

    @Test
    void testSearchInFile_NoMatch() throws IOException {
        Path filePath = TEST_DIR.resolve("test_no_match.txt");
        Files.write(filePath, "Nothing interesting here.".getBytes());

        DirectoryTraversal dt = new DirectoryTraversal();
        boolean result = dt.search_in_file(filePath.toString(), "Ben 10");

        assertFalse(result);
    }
}
