package tables;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

// Tests for RouteStop
class RouteStopTest {

    @Test
    void negativeIdThrows() {
        Location loc = new Location(1L, 0.0, 0.0, LocalDateTime.now());
        assertThrows(IllegalArgumentException.class,
                () -> new RouteStop(-5L, "Bad Stop", loc, LocalDateTime.now()));
    }
}
