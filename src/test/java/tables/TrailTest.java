package tables;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Tests for Trail

class TrailTest {

    // quick helper so tests don't repeat this setup
    private RouteStop sampleStop() {
        Location loc = new Location(1L, 53.0, -6.0, LocalDateTime.now());
        return new RouteStop(1L, "Stop A", loc, LocalDateTime.now());
    }

    @Test
    void blankNameThrows() {
        ArrayList<RouteStop> stops = new ArrayList<>();
        stops.add(sampleStop());

        assertThrows(IllegalArgumentException.class,
                () -> new Trail(1L, "   ", stops));
    }

    @Test
    void addStopsGrowsList() {
        ArrayList<RouteStop> stops = new ArrayList<>();
        stops.add(sampleStop());

        Trail trail = new Trail(1L, "Wicklow Way", stops);
        trail.addRoute_stops(sampleStop(), sampleStop());

        assertEquals(3, trail.getStops().size());
    }
}
