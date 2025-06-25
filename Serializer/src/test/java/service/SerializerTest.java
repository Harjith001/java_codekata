package service;

import factory.SerializerFactory;
import factory.SerializerType;
import model.Person;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class SerializerTest {

    private Person testPerson;
    private final String binaryFile = "test_person.ser";
    private final String jsonFile = "test_person.json";

    @BeforeEach
    void setUp() {
        testPerson = new Person("John Doe", 25, "456 Elm Street");
    }

    @AfterEach
    void tearDown() {
        new File(binaryFile).delete();
        new File(jsonFile).delete();
    }

    @Test
    void testBinarySerializationAndDeserialization() throws Exception {
        Serializer<Person> serializer = SerializerFactory.getSerializer(SerializerType.OBJECT);
        serializer.serialize(testPerson, binaryFile);

        Person deserializedPerson = serializer.deserialize(binaryFile, Person.class);

        assertNotNull(deserializedPerson);
        assertEquals(testPerson.getName(), deserializedPerson.getName());
        assertEquals(testPerson.getAge(), deserializedPerson.getAge());
        assertEquals(testPerson.getAddress(), deserializedPerson.getAddress());
    }

    @Test
    void testJsonSerializationAndDeserialization() throws Exception {
        Serializer<Person> serializer = SerializerFactory.getSerializer(SerializerType.JSON);
        serializer.serialize(testPerson, jsonFile);

        Person deserializedPerson = serializer.deserialize(jsonFile, Person.class);

        assertNotNull(deserializedPerson);
        assertEquals(testPerson.getName(), deserializedPerson.getName());
        assertEquals(testPerson.getAge(), deserializedPerson.getAge());
        assertEquals(testPerson.getAddress(), deserializedPerson.getAddress());
    }

    @Test
    void testInvalidFileDeserialization() {
        Serializer<Person> serializer = SerializerFactory.getSerializer(SerializerType.JSON);

        Exception exception = assertThrows(Exception.class, () -> {
            serializer.deserialize("non_existing_file.json", Person.class);
        });

        assertTrue(exception.getMessage().contains("non_existing_file.json"));
    }
}
