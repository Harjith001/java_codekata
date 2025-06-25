package config;

import org.junit.jupiter.api.*;
import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigManagerTest {

    static final String testDir = "test-resources/";

    @BeforeAll
    static void setup() {
        new File(testDir).mkdirs();
    }

    @Test
    void testPropertiesReadWrite() throws Exception {
        String path = testDir + "test-config.properties";
        Properties initial = new Properties();
        initial.setProperty("env", "test");

        ConfigWriter writer = ConfigFactory.getWriter(path);
        writer.writeConfig(path, initial);

        ConfigReader reader = ConfigFactory.getReader(path);
        Properties props = reader.readConfig(path);

        assertEquals("test", props.getProperty("env"));
    }

    @Test
    void testConfigManagerAddAndUpdate() throws Exception {
        String path = testDir + "test-config.properties";

        ConfigReader reader = ConfigFactory.getReader(path);
        ConfigWriter writer = ConfigFactory.getWriter(path);
        ConfigManager manager = new ConfigManager(reader, writer, path);

        manager.addProperty("test.key", "initialValue");
        assertEquals("initialValue", manager.getProperty("test.key"));

        manager.updateProperty("test.key", "updatedValue");
        assertEquals("updatedValue", manager.getProperty("test.key"));
    }

    @Test
    void testFactoryReturnsCorrectType() {
        assertInstanceOf(PropertiesConfig.class, ConfigFactory.getReader("file.properties"));
        assertInstanceOf(JsonConfig.class, ConfigFactory.getReader("file.json"));
        assertInstanceOf(XmlConfig.class, ConfigFactory.getReader("file.xml"));
        assertInstanceOf(YamlConfig.class, ConfigFactory.getReader("file.yaml"));
    }

    @Test
    void testYamlReadWrite() throws Exception {
        String path = testDir + "test-config.yaml";
        Properties props = new Properties();
        props.setProperty("yamlKey", "yamlValue");

        ConfigWriter writer = ConfigFactory.getWriter(path);
        writer.writeConfig(path, props);

        ConfigReader reader = ConfigFactory.getReader(path);
        Properties readProps = reader.readConfig(path);

        assertEquals("yamlValue", readProps.getProperty("yamlKey"));
    }

    @Test
    void testJsonReadWrite() throws Exception {
        String path = testDir + "test-config.json";
        Properties props = new Properties();
        props.setProperty("jsonKey", "jsonValue");

        ConfigWriter writer = ConfigFactory.getWriter(path);
        writer.writeConfig(path, props);

        ConfigReader reader = ConfigFactory.getReader(path);
        Properties readProps = reader.readConfig(path);

        assertEquals("jsonValue", readProps.getProperty("jsonKey"));
    }

    @Test
    void testXmlReadWrite() throws Exception {
        String path = testDir + "test-config.xml";
        Properties props = new Properties();
        props.setProperty("xmlKey", "xmlValue");

        ConfigWriter writer = ConfigFactory.getWriter(path);
        writer.writeConfig(path, props);

        ConfigReader reader = ConfigFactory.getReader(path);
        Properties readProps = reader.readConfig(path);

        assertEquals("xmlValue", readProps.getProperty("xmlKey"));
    }

    @Test
    void testFactoryThrowsForUnsupportedExtension() {
        assertThrows(IllegalArgumentException.class, () -> {
            ConfigFactory.getReader("file.unsupported");
        });
    }
}
