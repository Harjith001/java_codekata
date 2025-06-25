package logger;

public class ConsoleLogger extends Logger {
    @Override
    public void log(String message) {
        System.out.println("[Console] " + message);
    }
}
