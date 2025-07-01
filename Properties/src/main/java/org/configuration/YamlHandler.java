package org.configuration;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.*;

public class YamlHandler implements ConfigHandler {
    private Map<String, Object> configMap;
    private String filePath;
    private final Yaml yaml = new Yaml();

    @Override
    public void load(String filePath) throws IOException {
        this.filePath = filePath;
        File file = new File(filePath);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                configMap = yaml.load(input);
            }
        }
        if (configMap == null) configMap = new LinkedHashMap<>();
    }

    @Override
    public void printAll() {
        configMap.forEach((k, v) -> System.out.println(k + " = " + v));
    }

    @Override
    public void addProperty(String key, String value) throws IOException {
        configMap.put(key, value);
        save();
    }

    @Override
    public void updateProperty(String key, String newValue) throws IOException {
        if (configMap.containsKey(key)) {
            configMap.put(key, newValue);
            save();
        } else {
            System.out.println("Key not found.");
        }
    }

    private void save() throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            yaml.dump(configMap, writer);
        }
    }
}

