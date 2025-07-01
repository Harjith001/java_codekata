package utilities;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer<T> {
    void serialize(T obj, String filename) throws Exception;
    T deserialize(String filename, Class<T> clazz) throws Exception;

    void serialize(T obj, OutputStream outputStream) throws Exception;
    T deserialize(InputStream inputStream, Class<T> clazz) throws Exception;

}
