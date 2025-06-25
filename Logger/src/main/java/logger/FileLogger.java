package logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileLogger extends Logger {

    protected String filePath = "log.txt";

    public FileLogger() {}

    public FileLogger(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void log(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.println("[File] " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
