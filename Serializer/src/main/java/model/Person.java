package model;

import java.io.Serializable;

public class Person implements Serializable {

    private String name;
    private int age;
    private String address;

    public Person() {}

    public Person(String name, int age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    // Getters
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getAddress() { return address; }

    @Override
    public String toString() {
        return "Person{name='%s', age=%d, address='%s'}".formatted(name, age, address);
    }
}
