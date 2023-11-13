package Map.Occupiable;

import Map.Occupant.Crate;

/**
 * A destination tile. To win the game, we must push the crate with the corresponding ID onto this tile
 */
public class DestTile extends Tile {
    private char destID;

    /**
     * @param destID The destination uppercase char corresponding to a crate with the same lowercase letter
     */
    public DestTile(char destID) {
        this.destID = destID;
    }

    /**
     * @return Whether or not this destination tile has been completed, i.e. a crate with the matching lowercase letter
     * is currently occupying this tile.
     */
    public boolean isCompleted() {
        //TODO
        if (this.getOccupant().isPresent() && this.getOccupant().get() instanceof Crate)
            return Character.toUpperCase(((Crate) this.getOccupant().get()).getID()) == this.getDestID();
        return false;
    }

    /**
     * @return The uppercase letter corresponding to the crate with the matching lowercase letter
     */
    private char getDestID() {
        return destID;
    }

    @Override
    public char getRepresentation() {
        //TODO
        if (getOccupant().isPresent()) {
            return getOccupant().get().getRepresentation();
        } else {
            return getDestID();
        }
    }
}
