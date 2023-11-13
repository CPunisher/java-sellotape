import Exceptions.InvalidMapException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GameTest {

    Game g;

    @BeforeEach
    void setUp() throws InvalidMapException {
        g = new Game();
        g.loadMap("java/goodmap.txt");
    }

    @Test
    void loadMapFailure() throws InvalidMapException {
        g.loadMap("asdlfkjasdlkfj"); //should not throw exception
        assertThrows(InvalidMapException.class, () -> g.loadMap("java/badmap.txt"));
    }
}