package config;

public class ConfigFactory {

    public static ConfigReader getReader(String filePath) {
        String extension = getExtension(filePath);
        return switch (extension) {
            case "properties" -> new PropertiesConfig();
            case "xml"       -> new XmlConfig();
            case "json"      -> new JsonConfig();
            case "yaml", "yml" -> new YamlConfig();
            default -> throw new IllegalArgumentException("Unsupported file format: " + extension);
        };
    }

    public static ConfigWriter getWriter(String filePath) {
        String extension = getExtension(filePath);
        return switch (extension) {
            case "properties" -> new PropertiesConfig();
            case "xml"       -> new XmlConfig();
            case "json"      -> new JsonConfig();
            case "yaml", "yml" -> new YamlConfig();
            default -> throw new IllegalArgumentException("Unsupported file format: " + extension);
        };
    }

    private static String getExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filePath.length() - 1) {
            throw new IllegalArgumentException("Invalid file name or extension: " + filePath);
        }
        return filePath.substring(dotIndex + 1).toLowerCase();
    }
}
