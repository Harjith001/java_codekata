package factory;

import model.Person;

public class PersonFactory {
    public static Person createPerson(String name, int age, String address) {
        return new Person(name, age, address);
    }
}
