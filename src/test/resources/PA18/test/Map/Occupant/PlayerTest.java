package Map.Occupant;

import org.junit.jupiter.api.Test;

class PlayerTest {

    @Test
    void getRepresentation() {
        Player p = new Player(0, 0);
        assertEquals('@', p.getRepresentation());
    }
}