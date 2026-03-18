package utils;

import com.google.gson.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Util class for JSON Conversion
 *
 * @author Maryna Hordiienko
 *
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString()))
            .setPrettyPrinting()
            .create();

    public static <T> String toJson(T object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> String listToJson(List<T> list) {
        return gson.toJson(list);
    }
}