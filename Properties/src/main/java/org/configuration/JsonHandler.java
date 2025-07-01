package org.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

public class JsonHandler implements ConfigHandler {
    private Map<String, String> configMap;
    private final ObjectMapper mapper = new ObjectMapper();
    private String filePath;

    @Override
    public void load(String filePath) throws IOException {
        this.filePath = filePath;
        File file = new File(filePath);
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file)) {
                configMap = mapper.readValue(fis, Map.class);
            }
        } else {
            configMap = new HashMap<>();
        }
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
            throw new RuntimeException("missingKey");
        }
    }

    private void save() throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), configMap);
    }
}
