package org.example;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.Map;

public class YamlConfigHandler extends AbstractConfigHandler {
    private final Yaml yaml = new Yaml();

    public YamlConfigHandler(String filePath) throws Exception {
        super(filePath);
    }

    @Override
    protected void load(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(filePath)) {
            Object loaded = yaml.load(reader);
            if (loaded instanceof Map<?, ?> map) {
                map.forEach((k, v) -> props.setProperty(k.toString(), v.toString()));
            }
        }
    }

    @Override
    protected void saveToFile() throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            yaml.dump(props, writer);
        }
    }
}

