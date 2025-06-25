package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

abstract class Logger {

    public abstract String LOG(String message);
}

class FileLogger extends Logger {

    protected String filePath;

    public FileLogger(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String LOG(String message) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(message + System.lineSeparator());
            return message;
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + filePath);
            return "ERROR";
        }
    }
}

class ConsoleLogger extends Logger {

    private final PrintStream out;

    public ConsoleLogger(PrintStream out) {
        this.out = out;
    }

    @Override
    public String LOG(String message) {
        out.println(message);
        return message;
    }
}

class TimestampedFileLogger extends FileLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TimestampedFileLogger(String filePath) {
        super(filePath);
    }

    @Override
    public String LOG(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        super.LOG("[" + timestamp + "] " + message);
        return message;
    }
}

class LoggerManager {

    private Logger logger;

    public LoggerManager(Logger logger) {
        this.logger = logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void logMessage(String message) {
        logger.LOG(message);
    }
}

public class CustomLoggerSystem {

    public static void main(String[] args) {

        String logFile = "LOG.txt";
        String timestampedLogFile = "timestamped_log.txt";

        Logger fileLogger = new FileLogger(logFile);
        LoggerManager manager = new LoggerManager(fileLogger);
        manager.logMessage("This is a message to FileLogger.");

        Logger timestampedLogger = new TimestampedFileLogger(timestampedLogFile);
        manager.setLogger(timestampedLogger);
        manager.logMessage("This message has a timestamp.");

    }
}
