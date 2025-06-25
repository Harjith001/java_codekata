package config;
import java.util.Properties;

public class ConfigManager {

    private final ConfigWriter writer;
    private final String filePath;
    private final Properties props;

    public ConfigManager(ConfigReader reader, ConfigWriter writer, String filePath) throws Exception {
        this.writer = writer;
        this.filePath = filePath;
        this.props = reader.readConfig(filePath);
    }

    public void printAllProperties() {
        props.forEach((k, v) -> System.out.println(k + " = " + v));
    }

    public void addProperty(String key, String value) throws Exception {
        props.setProperty(key, value);
        writer.writeConfig(filePath, props);
    }

    public void updateProperty(String key, String newValue) throws Exception {
        if (props.containsKey(key)) {
            props.setProperty(key, newValue);
            writer.writeConfig(filePath, props);
        } else {
            System.out.println("Key not found: " + key);
        }
    }

    public String getProperty(String key) {
        return props.getProperty(key, null);
    }
}
