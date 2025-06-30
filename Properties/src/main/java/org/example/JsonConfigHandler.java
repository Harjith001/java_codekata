package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.Map;

public class JsonConfigHandler extends AbstractConfigHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonConfigHandler(String filePath) throws Exception {
        super(filePath);
    }

    @Override
    protected void load(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Map<String, Object> map = mapper.readValue(reader, Map.class);
            map.forEach((k, v) -> props.setProperty(k, v.toString()));
        }
    }

    @Override
    protected void saveToFile() throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, props);
        }
    }
}

