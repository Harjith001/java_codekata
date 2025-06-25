package service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class JsonSerializer<T> implements Serializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void serialize(T obj, String filename) throws Exception {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), obj);
    }

    @Override
    public T deserialize(String filename, Class<T> clazz) throws Exception {
        return objectMapper.readValue(new File(filename), clazz);
    }
}
