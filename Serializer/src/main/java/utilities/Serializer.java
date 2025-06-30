package utilities;

public interface Serializer<T> {
    void serialize(T obj, String filename) throws Exception;
    T deserialize(String filename, Class<T> clazz) throws Exception;
}
