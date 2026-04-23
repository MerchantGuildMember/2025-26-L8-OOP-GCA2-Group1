package utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for JsonUtil
 */
class JsonUtilTest {

    @Test
    void objectToJson() {
        SimpleBean bean = new SimpleBean("Trail-1", 5);
        String json = JsonUtil.toJson(bean);

        assertNotNull(json);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("Trail-1"));
        assertTrue(json.contains("\"count\""));
        assertTrue(json.contains("5"));
    }

    @Test
    void listToJsonWorks() {
        List<String> stops = Arrays.asList("Start", "Middle", "End");
        String json = JsonUtil.listToJson(stops);

        assertNotNull(json);
        assertTrue(json.trim().startsWith("["));
        assertTrue(json.trim().endsWith("]"));
        assertTrue(json.contains("Start") && json.contains("Middle") && json.contains("End"));
    }

    private static class SimpleBean {
        private final String name;
        private final int count;

        SimpleBean(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }
}
