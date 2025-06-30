package org.example;

import java.util.Properties;

public abstract class AbstractConfigHandler implements ConfigHandler {
    protected final String filePath;
    protected final Properties props = new Properties();

    protected AbstractConfigHandler(String filePath) throws Exception {
        this.filePath = filePath;
        load(filePath);
    }

    protected abstract void load(String filePath) throws Exception;
    protected abstract void saveToFile() throws Exception;

    @Override
    public String get(String key) {
        return props.getProperty(key);
    }

    @Override
    public void set(String key, String value) {
        props.setProperty(key, value);
    }

    @Override
    public boolean contains(String key) {
        return props.containsKey(key);
    }

    @Override
    public void printAll() {
        props.forEach((k, v) -> System.out.println(k + " = " + v));
    }

    @Override
    public void save() throws Exception {
        saveToFile();
    }
}
