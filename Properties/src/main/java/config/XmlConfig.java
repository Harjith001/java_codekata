package config;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class XmlConfig implements ConfigReader, ConfigWriter {
    @Override
    public Properties readConfig(String filePath) throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(filePath)) {
            props.loadFromXML(in);
        }
        return props;
    }

    @Override
    public void writeConfig(String filePath, Properties props) throws IOException {
        try (OutputStream out = new FileOutputStream(filePath)) {
            props.storeToXML(out, "Updated XML config", StandardCharsets.UTF_8);
        }
    }
}
