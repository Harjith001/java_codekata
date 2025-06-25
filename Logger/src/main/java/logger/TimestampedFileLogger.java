package logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampedFileLogger extends FileLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TimestampedFileLogger() {
        super("timestamped-log.txt");
    }

    @Override
    public void log(String message) {
        String timestampedMessage = "[" + LocalDateTime.now().format(formatter) + "] " + message;
        super.log(timestampedMessage);
    }
}
