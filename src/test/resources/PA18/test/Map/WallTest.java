package Map;

import org.junit.jupiter.api.Test;

class WallTest {

    @Test
    void
    getRepresentation() {
        Wall w = new Wall();
        assertEquals('#', w.getRepresentation());
    }
}