package org.example;

public class ConfigFactory {
    public static ConfigHandler create(String filePath) throws Exception {
        String ext = getExtension(filePath);
        return switch (ext) {
            case "json" -> new JsonConfigHandler(filePath);
            case "yaml", "yml" -> new YamlConfigHandler(filePath);
            case "xml" -> new XmlConfigHandler(filePath);
            case "properties" -> new PropertiesConfigHandler(filePath);
            default -> throw new IllegalArgumentException("Unsupported config format: " + ext);
        };
    }

    private static String getExtension(String filePath) {
        int dot = filePath.lastIndexOf('.');
        if (dot == -1 || dot == filePath.length() - 1) {
            throw new IllegalArgumentException("Invalid file name: " + filePath);
        }
        return filePath.substring(dot + 1).toLowerCase();
    }
}

