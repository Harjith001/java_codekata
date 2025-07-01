package org.configuration;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonHandlerTest {

    private JsonHandler handler;
    private Path tempJsonFile;

    @BeforeEach
    void setUp() throws Exception {
        handler = new JsonHandler();
        tempJsonFile = Files.createTempFile("test-config", ".json");
        handler.load(tempJsonFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempJsonFile);
    }

    @Test
    void testAddAndReadProperty() throws Exception {
        handler.addProperty("username", "admin");

        JsonHandler reloaded = new JsonHandler();
        reloaded.load(tempJsonFile.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        reloaded.printAll();
        System.setOut(originalOut);

        String output = out.toString();
        assertTrue(output.contains("username = admin"));
    }

    @Test
    void testUpdateExistingProperty() throws Exception {
        handler.addProperty("port", "8080");
        handler.updateProperty("port", "9090");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        handler.printAll();
        System.setOut(System.out);

        assertTrue(out.toString().contains("port = 9090"));
    }

    @Test
    void testUpdateMissingProperty() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            handler.updateProperty("missingKey", "value");
        });

        assertTrue(exception.getMessage().contains("missingKey"));
    }

}
