package org.example;

public interface ConfigHandler {
    String get(String key);
    void set(String key, String value);
    boolean contains(String key);
    void save() throws Exception;
    void printAll();
}
