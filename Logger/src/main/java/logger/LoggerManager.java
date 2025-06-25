package logger;

public class LoggerManager {

    private Logger logger;

    public LoggerManager(Logger logger) {
        this.logger = logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void log(String message) {
        logger.log(message);
    }
}
