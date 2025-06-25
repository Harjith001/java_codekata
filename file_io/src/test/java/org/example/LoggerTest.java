package org.example;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    private File tempFile;

    @BeforeEach
    void setup() throws IOException {
        tempFile = File.createTempFile("log", ".txt");
        tempFile.deleteOnExit();
    }

    @Test
    void testFileLoggerWritesMessage() throws IOException {
        FileLogger logger = new FileLogger(tempFile.getAbsolutePath());
        String testMessage = "Hello, FileLogger!";
        String returned = logger.LOG(testMessage);

        assertEquals(testMessage, returned);

        String content = Files.readString(tempFile.toPath());
        assertTrue(content.contains(testMessage), "Log file should contain the test message");
    }

    @Test
    void TimestampTest() throws IOException {
        TimestampedFileLogger logger = new TimestampedFileLogger(tempFile.getAbsolutePath());
        String testMessage = "Timestamped log message";

        logger.LOG(testMessage);

        String content = Files.readString(tempFile.toPath());

        assertTrue(content.contains(testMessage), "Log file should contain the test message");

        assertTrue(content.matches("(?s).*\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\] .*"),
                "Log file should contain timestamp in format [YYYY-MM-DD HH:mm:ss]");
    }

    @Test
    void testConsole() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream testPrintStream = new PrintStream(outContent);

        ConsoleLogger logger = new ConsoleLogger(testPrintStream);

        String message = "Hello ConsoleLogger";
        String returned = logger.LOG(message);

        assertEquals(message, returned);
        System.out.print(outContent);

        assertTrue(outContent.toString().contains(message), "Console output should contain the logged message");
    }
}
