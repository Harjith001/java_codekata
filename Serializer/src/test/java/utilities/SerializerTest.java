package utilities;

import factory.SerializerFactory;
import factory.SerializerType;
import model.Person;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SerializerTest {

    private Person testPerson;
    private final String objectFile = "test_person.ser";
    private final String jsonFile = "test_person.json";

    @BeforeEach
    void setUp() {
        testPerson = new Person("John Doe", 25, "456 Elm Street");
    }

    @AfterEach
    void tearDown() {
        new File(objectFile).delete();
        new File(jsonFile).delete();
    }

    @Test
    void testSerializationAndDeserialization() throws Exception {
        Serializer<Person> serializer = SerializerFactory.getSerializer(SerializerType.OBJECT);
        serializer.serialize(testPerson, objectFile);

        Person deserializedPerson = serializer.deserialize(objectFile, Person.class);

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

    @Test
    void testSerializationWithStreamsOnly() throws Exception {
        Serializer<Person> serializer = SerializerFactory.getSerializer(SerializerType.OBJECT);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(testPerson, outputStream);

        byte[] serializedData = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedData);

        Person deserializedPerson = serializer.deserialize(inputStream, Person.class);

        assertNotNull(deserializedPerson);
        assertEquals(testPerson.getName(), deserializedPerson.getName());
        assertEquals(testPerson.getAge(), deserializedPerson.getAge());
        assertEquals(testPerson.getAddress(), deserializedPerson.getAddress());
    }
}
