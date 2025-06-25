package org.example;

import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class SerialTest {

    private Serial serial;

    @BeforeEach
    void setUp() {
        serial = new Serial();
        File tempFile = new File("test_person.ser");
        tempFile.deleteOnExit();
    }

    @Test
    void testSerializeAndDeserialize() {
        Serial.Person original = new Serial.Person("Harjith", 21, "Chennai");

        serial.serialize(original);
        Serial.Person deserialized = serial.deserialize("person.ser");

        assertNotNull(deserialized);
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getAge(), deserialized.getAge());
        assertEquals(original.getAddress(), deserialized.getAddress());
    }

    @Test
    void testDeserializeWithInvalidPath() {
        Serial.Person deserialized = serial.deserialize("invalid_path.ser");
        assertNull(deserialized);
    }
}
