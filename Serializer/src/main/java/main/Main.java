package main;

import factory.PersonFactory;
import factory.SerializerType;
import model.Person;
import service.*;
import factory.SerializerFactory;

public class Main {
    public static void main(String[] args) {
        try {
            Person person = PersonFactory.createPerson("Alice", 30, "123 Main St");

            Serializer<Person> binarySerializer = SerializerFactory.getSerializer(SerializerType.OBJECT);
            binarySerializer.serialize(person, "person.ser");
            Person deserializedBinary = binarySerializer.deserialize("person.ser", Person.class);
            System.out.println("Binary Deserialized: " + deserializedBinary);

            Serializer<Person> jsonSerializer = SerializerFactory.getSerializer(SerializerType.JSON);
            jsonSerializer.serialize(person, "person.json");
            Person deserializedJson = jsonSerializer.deserialize("person.json", Person.class);
            System.out.println("JSON Deserialized: " + deserializedJson);

        } catch (Exception e) {
            System.out.println("Exception occurred while serialization");
        }
    }
}
