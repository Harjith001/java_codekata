package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    @Override
    public void serialize(T obj, OutputStream outputStream) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, obj);
    }

    @Override
    public T deserialize(InputStream inputStream, Class<T> clazz) throws IOException {
        return objectMapper.readValue(inputStream, clazz);
    }

}
