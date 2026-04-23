package tables;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


// Tests for Location

class LocationTest {

    @Test
    void constructorSetsFields() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        Location loc = new Location(1L, 53.3498, -6.2603, "Dublin, Ireland", now);

        assertEquals(1L, loc.getId());
        assertEquals("Dublin, Ireland", loc.getFullAddress());
        assertEquals(now, loc.getCreationTime());
    }

    @Test
    void negativeIdThrows() {
        LocalDateTime now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class,
                () -> new Location(-1L, 0.0, 0.0, now));
    }
}
