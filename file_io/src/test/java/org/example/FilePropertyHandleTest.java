package org.example;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FilePropertyHandleTest {

    private static Path tempFile;
    private Properties props;
    private FilePropertyHandle fp;

    @BeforeEach
    void setup() throws IOException {
        tempFile = Files.createTempFile("testconfig", ".properties");

        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            writer.write("db.url=InitialValue\n");
            writer.write("user.name=Tester\n");
        }

        props = new Properties();
        try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
            props.load(fis);
        }

        fp = new FilePropertyHandle();
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(tempFile);
    }


    @Test
    void testSetProperty() throws IOException {
        String newValue = "NewDBValue";

        fp.set_property(props, "db.url", newValue, tempFile.toString());

        Properties reloaded = new Properties();
        try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
            reloaded.load(fis);
        }

        assertEquals(newValue, reloaded.getProperty("db.url"));
    }
    
}
