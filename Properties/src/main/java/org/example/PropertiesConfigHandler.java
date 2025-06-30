package org.example;

import java.io.*;

public class PropertiesConfigHandler extends AbstractConfigHandler {

    public PropertiesConfigHandler(String filePath) throws Exception {
        super(filePath);
    }

    @Override
    protected void load(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (InputStream in = new FileInputStream(file)) {
            props.load(in);
        }
    }

    @Override
    protected void saveToFile() throws IOException {
        try (OutputStream out = new FileOutputStream(filePath)) {
            props.store(out, "Updated properties config");
        }
    }
}
