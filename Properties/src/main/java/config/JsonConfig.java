package config;
import java.io.*;
import java.util.Properties;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConfig implements ConfigReader, ConfigWriter {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Properties readConfig(String filePath) throws IOException {
        Properties props = new Properties();
        try (Reader reader = new FileReader(filePath)) {
            var map = mapper.readValue(reader, java.util.Map.class);
            props.putAll(map);
        }
        return props;
    }

    @Override
    public void writeConfig(String filePath, Properties props) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, props);
        }
    }
}
