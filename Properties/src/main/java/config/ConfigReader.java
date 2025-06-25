package config;
import java.util.Properties;

public interface ConfigReader {
    Properties readConfig(String filePath) throws Exception;
}
