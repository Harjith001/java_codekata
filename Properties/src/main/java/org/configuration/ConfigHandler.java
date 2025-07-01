package org.configuration;

public interface ConfigHandler {
    void load(String filePath) throws Exception;
    void printAll();
    void addProperty(String key, String value) throws Exception;
    void updateProperty(String key, String newValue) throws Exception;
}
