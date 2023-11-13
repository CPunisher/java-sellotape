package game.map.cells;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import util.Coordinate;
import util.Direction;
import util.PipePatterns;

class TerminationCellTest {

    private static Coordinate DEFAULT_COORD = new Coordinate(1, 2);
    private static Direction DEFAULT_DIR = Direction.UP;
    private static TerminationCell.Type DEFAULT_TYPE = TerminationCell.Type.SOURCE;

    private TerminationCell cell = null;

    @Test
    void givenCell_assertCorrectProperties() {
        cell = new TerminationCell(DEFAULT_COORD, DEFAULT_DIR, DEFAULT_TYPE);

        assertEquals(DEFAULT_COORD, cell.coord);
        assertEquals(DEFAULT_DIR, cell.pointingTo);
        assertEquals(DEFAULT_TYPE, cell.type);
    }

    @Test
    void givenCell_assertSingleCharRepresentation() {
        cell = new TerminationCell(DEFAULT_COORD, DEFAULT_DIR, DEFAULT_TYPE);

        assertEquals(PipePatterns.Unfilled.UP_ARROW, cell.toSingleChar());
    }

    @Test
    void givenFilledCell_assertSingleCharRepresentation() {
        cell = new TerminationCell(DEFAULT_COORD, DEFAULT_DIR, DEFAULT_TYPE);
        cell.setFilled();

        assertEquals(PipePatterns.Filled.UP_ARROW, cell.toSingleChar());
    }

    @AfterEach
    void tearDown() {
        cell = null;
    }
}
