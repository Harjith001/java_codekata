package factory;

import utilities.FileSerializer;
import utilities.JsonSerializer;
import utilities.Serializer;

import java.io.Serializable;

public class SerializerFactory {
    public static <T extends Serializable> Serializer<T> getSerializer(SerializerType type) {
        return switch (type) {
            case OBJECT -> new FileSerializer<>();
            case JSON -> new JsonSerializer<>();
        };
    }
}
