package tables;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Tests for TrailMedia

class TrailMediaTest {

    @Test
    void nullUrlThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new TrailMedia(1L, 1L, 1L, "image",
                        null, "caption", LocalDateTime.now()));
    }

    @Test
    void fileNameTrimming() {
        TrailMedia media = new TrailMedia(1L, 1L, null, "image",
                "http://example.com/a.png", "cap", LocalDateTime.now());

        media.setFFileName("   photo.png   ");
        assertEquals("photo.png", media.getFFileName());

        // null should come back as empty string
        media.setFFileName(null);
        assertEquals("", media.getFFileName());
    }
}
