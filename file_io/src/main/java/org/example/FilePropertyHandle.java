package org.example;

import java.io.*;
import java.util.Properties;

public class FilePropertyHandle {

    private static final String path = "config.properties";

    public void display_property(Properties prop, String property) {

        String p = prop.getProperty(property);
        System.out.println(p);
    }

    public void set_property(Properties prop, String key, String new_value, String path) {

        prop.setProperty(key, new_value);

        try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(path))) {
            prop.store(fos, "Property file has been updated");
        } catch (IOException e) {
            System.out.println("Cant open the file");
        }
    }

    public static void main(String[] args) {
        Properties prop = new Properties();
        String key = "db.url";
        try (FileInputStream fs = new FileInputStream(path)) {
            prop.load(fs);
        } catch (IOException e) {
            System.out.println("Error reading properties");
        }
        FilePropertyHandle fp = new FilePropertyHandle();
        fp.display_property(prop, key);
        fp.set_property(prop, key, "Greninja", path);
    }
}
