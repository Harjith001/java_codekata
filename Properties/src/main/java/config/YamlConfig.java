
package config;
import java.io.*;
import java.util.Properties;
import org.yaml.snakeyaml.Yaml;

public class YamlConfig implements ConfigReader, ConfigWriter {
    private final Yaml yaml = new Yaml();

    @Override
    public Properties readConfig(String filePath) throws IOException {
        Properties props = new Properties();
        try (Reader reader = new FileReader(filePath)) {
            Object map = yaml.load(reader);
            if (map instanceof java.util.Map) {
                ((java.util.Map<?, ?>) map).forEach((k, v) -> props.setProperty(k.toString(), v.toString()));
            }
        }
        return props;
    }

    @Override
    public void writeConfig(String filePath, Properties props) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            yaml.dump(props, writer);
        }
    }
}
