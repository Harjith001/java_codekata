package org.example;

import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Serial implements Serializable {
    
    private static final Logger LOG = LogManager.getLogger(Serial.class);

    public static final class Person implements Serializable {
        private final String name;
        private final int age;
        private final String address;

        Person(String name, int age, String address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        public String getName(){
            return name;
        }
        public int getAge(){
            return age;
        }
        public String getAddress(){
            return address;
        }
    }

    void serialize(Person p) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("person.ser"))) {
            out.writeObject(p);
            LOG.info("Object Person is serialized");
        } catch (IOException e) {
            LOG.error("File IO exception for serialize");
        }
    }

    Person deserialize(String path) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            Person obj = (Person) in.readObject();
            LOG.info(obj);
            return obj;
        } catch (IOException e) {
            LOG.error("File IO exception");
            return null;
        } catch (ClassNotFoundException e) {
            LOG.error("Class not found");
            return null;
        }
    }

    public static void main(String[] args) {
        Serial s = new Serial();
        Person p = new Person("Harjith", 21, "Chennai");
        s.serialize(p);
        Person newP = s.deserialize("/home/harjith/IdeaProjects/file_io/person.ser");
    }
}
