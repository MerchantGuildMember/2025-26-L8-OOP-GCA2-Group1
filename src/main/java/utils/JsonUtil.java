package utils;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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