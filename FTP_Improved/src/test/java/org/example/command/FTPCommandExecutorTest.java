package org.example.command;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.example.connection.ClientSession;
import org.example.parser.FTPCommandLexer;
import org.example.parser.FTPCommandParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FTPCommandExecutorTest {

    @Mock
    private ClientSession mockSession;

    @TempDir
    Path tempDir;

    private FTPCommandExecutor executor;
    private File serverDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create temporary server directory
        serverDir = tempDir.resolve("server_directory").toFile();
        serverDir.mkdirs();

        executor = new FTPCommandExecutor();

        // Mock session default behavior
        when(mockSession.getMaxFileSize()).thenReturn(100 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() {
        // Clean up test files
        if (serverDir.exists()) {
            File[] files = serverDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

    // Helper method to parse command
    private ParseTree parseCommand(String command) throws IOException {
        CharStream charStream = CharStreams.fromReader(new StringReader(command));
        FTPCommandLexer lexer = new FTPCommandLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FTPCommandParser parser = new FTPCommandParser(tokens);
        return parser.command();
    }

    @Test
    void testLoginSuccess() throws IOException {
        // Given
        ParseTree tree = parseCommand("LOGIN user pass");

        // When
        String result = executor.execute(tree, mockSession);

        // Then
        assertTrue(result.contains("Content-Length:"));
        assertTrue(result.contains("230 Login successful."));
    }

    @Test
    void testLoginFailure() throws IOException {
        // Given
        ParseTree tree = parseCommand("LOGIN wronguser wrongpass");

        // When
        String result = executor.execute(tree, mockSession);

        // Then
        assertTrue(result.contains("Content-Length:"));
        assertTrue(result.contains("530 Login failed. Invalid credentials."));
    }

    @Test
    void testCommandWithoutLogin() throws IOException {
        // Given
        ParseTree tree = parseCommand("LIST");

        // When
        String result = executor.execute(tree, mockSession);

        // Then
        assertTrue(result.contains("530 Not authenticated. Please LOGIN first."));
    }


    @Test
    void testGetExistingFile() throws IOException {
        // Given - create test file
        File testFile = new File(serverDir, "test.txt");
        String fileContent = "Hello World!";
        Files.write(testFile.toPath(), fileContent.getBytes());

        // Login first
        ParseTree loginTree = parseCommand("LOGIN user pass");
        executor.execute(loginTree, mockSession);

        ParseTree getTree = parseCommand("GET test.txt");

        // When
        String result = executor.execute(getTree, mockSession);

        // Then
        assertTrue(result.contains("226 File transfer successful."));
        assertTrue(result.contains(fileContent));
    }

    @Test
    void testGetNonExistentFile() throws IOException {
        // Given - login first
        ParseTree loginTree = parseCommand("LOGIN user pass");
        executor.execute(loginTree, mockSession);

        ParseTree getTree = parseCommand("GET nonexistent.txt");

        // When
        String result = executor.execute(getTree, mockSession);

        // Then
        assertTrue(result.contains("550 Error: File not found."));
    }

    @Test
    void testPutCommandValid() throws IOException {
        // Given - login first
        ParseTree loginTree = parseCommand("LOGIN user pass");
        executor.execute(loginTree, mockSession);

        ParseTree putTree = parseCommand("PUT test.txt 100");

        // When
        String result = executor.execute(putTree, mockSession);

        // Then
        assertEquals("file_upload_ready", result);
        verify(mockSession).startFileUpload("test.txt", 100);
    }

    @Test
    void testPutCommandInvalidSize() throws IOException {
        // Given - login first
        ParseTree loginTree = parseCommand("LOGIN user pass");
        executor.execute(loginTree, mockSession);

        ParseTree putTree = parseCommand("PUT test.txt 0");

        // When
        String result = executor.execute(putTree, mockSession);

        // Then
        assertTrue(result.contains("550 Invalid file size"));
    }

    @Test
    void testPutCommandFileSizeExceedsLimit() throws IOException {
        // Given - login first
        ParseTree loginTree = parseCommand("LOGIN user pass");
        executor.execute(loginTree, mockSession);

        when(mockSession.getMaxFileSize()).thenReturn(1000);
        ParseTree putTree = parseCommand("PUT test.txt 2000");

        // When
        String result = executor.execute(putTree, mockSession);

        // Then
        assertTrue(result.contains("552 File size exceeds maximum allowed size"));
    }

    @Test
    void testPutCommandSessionThrowsException() throws IOException {
        // Given - login first
        ParseTree loginTree = parseCommand("LOGIN user pass");
        executor.execute(loginTree, mockSession);

        doThrow(new IOException("Session error")).when(mockSession).startFileUpload(anyString(), anyInt());
        ParseTree putTree = parseCommand("PUT test.txt 100");

        // When
        String result = executor.execute(putTree, mockSession);

        // Then
        assertTrue(result.contains("550 Error starting file upload: Session error"));
    }

    @Test
    void testQuitCommand() throws IOException {
        // Given - login first
        ParseTree loginTree = parseCommand("LOGIN user pass");
        executor.execute(loginTree, mockSession);

        ParseTree quitTree = parseCommand("QUIT");

        // When
        String result = executor.execute(quitTree, mockSession);

        // Then
        assertEquals("abort", result);
    }

    @Test
    void testSaveFileSuccess() throws IOException {
        // Given
        String filename = "test.txt";
        byte[] data = "Hello World!".getBytes();

        // When
        String result = executor.saveFile(filename, data);

        // Then
        assertTrue(result.contains("226 File uploaded successfully: test.txt (12 bytes)"));

    }


    @Test
    void testFormatResponse() throws IOException {
        // Given - login first to access formatting
        ParseTree loginTree = parseCommand("LOGIN user pass");
        String result = executor.execute(loginTree, mockSession);

        // Then - verify response format
        assertTrue(result.startsWith("Content-Length: "));
        assertTrue(result.contains("\r\n\r\n"));
        assertTrue(result.contains("230 Login successful."));

        // Verify content length is correct
        String[] parts = result.split("\r\n\r\n", 2);
        String header = parts[0];
        String content = parts[1];

        String lengthStr = header.substring("Content-Length: ".length());
        int expectedLength = Integer.parseInt(lengthStr);
        assertEquals(expectedLength, content.length());
    }

}
