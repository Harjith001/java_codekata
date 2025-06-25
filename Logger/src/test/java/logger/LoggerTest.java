package logger;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class LoggerTest {

    private final static String TEST_LOG_FILE = "test-log.txt";
    private final static String TEST_TIMESTAMPED_LOG_FILE = "timestamped-log.txt";

    @BeforeEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(Path.of(TEST_LOG_FILE));
        Files.deleteIfExists(Path.of(TEST_TIMESTAMPED_LOG_FILE));
    }

    @Test
    void testConsoleLoggerOutputsCorrectly() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        ConsoleLogger logger = new ConsoleLogger();
        logger.log("Hello Console");

        System.setOut(originalOut);

        String output = outputStream.toString().trim();
        assertEquals("[Console] Hello Console", output);
    }

    @Test
    void testFileLoggerWritesToFile() throws IOException {
        FileLogger logger = new FileLogger(TEST_LOG_FILE);
        logger.log("Hello File");

        assertTrue(Files.exists(Path.of(TEST_LOG_FILE)));

        String content = Files.readString(Path.of(TEST_LOG_FILE)).trim();
        assertEquals("[File] Hello File", content);
    }

    @Test
    void testTimestampedFileLoggerAddsTimestamp() throws IOException {
        TimestampedFileLogger logger = new TimestampedFileLogger();

        logger.filePath = TEST_TIMESTAMPED_LOG_FILE;

        logger.log("Hello Timestamp");

        String content = Files.readString(Path.of(TEST_TIMESTAMPED_LOG_FILE)).trim();

        // Format : [2025-06-23 18:45:30] Hello Timestamp
        assertTrue(content.matches("\\[File] \\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}] Hello Timestamp"));

    }

    @Test
    void testLoggerManagerSwitchingLoggers() throws IOException {
        LoggerManager manager = new LoggerManager(new FileLogger(TEST_LOG_FILE));
        manager.log("File message");

        String fileContent = Files.readString(Path.of(TEST_LOG_FILE)).trim();
        assertEquals("[File] File message", fileContent);

        // Switch to console logger and capture output
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        manager.setLogger(new ConsoleLogger());
        manager.log("Console message");

        System.setOut(originalOut);

        String consoleOutput = outputStream.toString().trim();
        assertEquals("[Console] Console message", consoleOutput);
    }
}
