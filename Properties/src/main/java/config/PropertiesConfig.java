package config;
import java.io.*;
import java.util.Properties;


public class PropertiesConfig implements ConfigReader, ConfigWriter {
    @Override
    public Properties readConfig(String filePath) throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(filePath)) {
            props.load(in);
        }
        return props;
    }


    @Override
    public void writeConfig(String filePath, Properties props) throws IOException {
        try (OutputStream out = new FileOutputStream(filePath)) {
            props.store(out, "Updated properties config");
        }
    }
}
