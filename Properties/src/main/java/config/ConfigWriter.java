package config;
import java.util.Properties;

public interface ConfigWriter {
    void writeConfig(String filePath, Properties props) throws Exception;
}
