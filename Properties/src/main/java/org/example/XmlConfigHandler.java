package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class XmlConfigHandler extends AbstractConfigHandler {

    public XmlConfigHandler(String filePath) throws Exception {
        super(filePath);
    }

    @Override
    protected void load(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (InputStream in = new FileInputStream(file)) {
            props.loadFromXML(in);
        }
    }

    @Override
    protected void saveToFile() throws IOException {
        try (OutputStream out = new FileOutputStream(filePath)) {
            props.storeToXML(out, "Updated XML config", StandardCharsets.UTF_8);
        }
    }
}
