package org.configuration;

import java.io.*;
import java.util.*;

public class XmlHandler implements ConfigHandler {
    private final Properties props = new Properties();
    private String filePath;

    @Override
    public void load(String filePath) throws IOException {
        this.filePath = filePath;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.loadFromXML(fis);
        }
    }

    @Override
    public void printAll() {
        props.forEach((k, v) -> System.out.println(k + " = " + v));
    }

    @Override
    public void addProperty(String key, String value) throws IOException {
        props.setProperty(key, value);
        save();
    }

    @Override
    public void updateProperty(String key, String newValue) throws IOException {
        if (props.containsKey(key)) {
            props.setProperty(key, newValue);
            save();
        } else {
            System.out.println("Key not found.");
        }
    }

    private void save() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            props.storeToXML(fos, "Updated XML Config");
        }
    }
}
