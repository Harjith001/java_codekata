package utilities;

import java.io.*;

public class FileSerializer<T extends Serializable> implements Serializer<T> {
    @Override
    public void serialize(T obj, String filename) throws IOException {
        serialize(obj, new FileOutputStream(filename));
    }

    @Override
    public T deserialize(String filename, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return clazz.cast(ois.readObject());
        }
    }

    @Override
    public void serialize(T obj, OutputStream outputStream) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
            oos.writeObject(obj);
            oos.flush();
        }
    }

    @Override
    public T deserialize(InputStream inputStream, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
            return clazz.cast(ois.readObject());
        }
    }
}
