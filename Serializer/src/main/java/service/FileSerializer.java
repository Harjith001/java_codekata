package service;

import java.io.*;

public class FileSerializer<T extends Serializable> implements Serializer<T> {
    @Override
    public void serialize(T obj, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(obj);
        }
    }

    @Override
    public T deserialize(String filename, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return clazz.cast(ois.readObject());
        }
    }
}
