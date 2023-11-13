import Exceptions.InvalidMapException;
import Map.*;
import Map.Occupant.Crate;
import Map.Occupiable.DestTile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Holds the necessary components for running the game.
 */
public class Game {

    private Map m;
    private int numRows;
    private int numCols;
    private char[][] rep;

    /**
     * Loads and reads the map line by line, instantiates and initializes Map m.
     * Print out the number of rows, then number of cols (on two separate lines).
     *
     * @param filename the map text filename
     * @throws InvalidMapException
     */
    public void loadMap(String filename) throws InvalidMapException {
        try {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);

            numRows = Integer.parseInt(scanner.nextLine());
            numCols = Integer.parseInt(scanner.nextLine());

            rep = new char[numRows][numCols];

            for (int i = 0; i < numRows; i++) {
                String line = scanner.nextLine();
                for (int j = 0; j < numCols; j++) {
                    rep[i][j] = line.charAt(j);
                }
            }

            m = new Map();
            m.initialize(numRows, numCols, rep);

            scanner.close();
        } catch (FileNotFoundException e) {
//            throw new InvalidMapException("Map file not found");
        }
    }

    /**
     * Can be done using functional concepts.
     * @return Whether or not the win condition has been satisfied
     */
    public boolean isWin() {
        for (DestTile destTile : m.getDestTiles()) {
            if (!destTile.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    /**
     * When no crates can be moved but the game is not won, then deadlock has occurred.
     *
     * @return Whether deadlock has occurred
     */
    public boolean isDeadlocked() {
        for (Crate crate : m.getCrates()) {
            int r = crate.getR();
            int c = crate.getC();

            boolean upBlocked = m.isOccupiableAndNotOccupiedWithCrate(r - 1, c) && m.getCells()[r - 1][c] instanceof Wall;
            boolean downBlocked = m.isOccupiableAndNotOccupiedWithCrate(r + 1, c) && m.getCells()[r + 1][c] instanceof Wall;
            boolean leftBlocked = m.isOccupiableAndNotOccupiedWithCrate(r, c - 1) && m.getCells()[r][c - 1] instanceof Wall;
            boolean rightBlocked = m.isOccupiableAndNotOccupiedWithCrate(r, c + 1) && m.getCells()[r][c + 1] instanceof Wall;

            if (upBlocked && downBlocked && leftBlocked && rightBlocked) {
                return true;
            }
        }
        return false;
    }

    /**
     * Print the map to console
     */
    public void display() {
        Cell[][] cells = m.getCells();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                System.out.print(cells[i][j].getRepresentation());
            }
            System.out.println();
        }
    }

    /**
     * @param c The char corresponding to a move from the user
     *          w: up
     *          a: left
     *          s: down
     *          d: right
     *          r: reload map (resets any progress made so far)
     * @return Whether or not the move was successful
     */
    public boolean makeMove(char c) {
        Map.Direction direction = null;
        switch (c) {
            case 'w':
                direction = Map.Direction.UP;
                break;
            case 'a':
                direction = Map.Direction.LEFT;
                break;
            case 's':
                direction = Map.Direction.DOWN;
                break;
            case 'd':
                direction = Map.Direction.RIGHT;
                break;
            case 'r':
                try {
                    m.initialize(numRows, numCols, rep);
                    return true;
                } catch (InvalidMapException e) {
                    return false;
                }
        }

        if (direction != null) {
            return m.movePlayer(direction);
        }

        return false;
    }
}
