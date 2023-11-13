package hk.ust.comp3021.game;

import hk.ust.comp3021.actions.*;
import hk.ust.comp3021.entities.Box;
import hk.ust.comp3021.entities.Empty;
import hk.ust.comp3021.entities.Player;
import hk.ust.comp3021.entities.Wall;
import hk.ust.comp3021.utils.ShouldNotReachException;
import org.jetbrains.annotations.NotNull;

import static hk.ust.comp3021.utils.StringResources.PLAYER_NOT_FOUND;
import static hk.ust.comp3021.utils.StringResources.UNDO_QUOTA_RUN_OUT;

/**
 * A base implementation of Sokoban Game.
 */
public abstract class AbstractSokobanGame implements SokobanGame {
    @NotNull
    protected final GameState state;

    private boolean isExitSpecified = false;

    protected AbstractSokobanGame(@NotNull GameState gameState) {
        this.state = gameState;
    }

    /**
     * @return True is the game should stop running.
     * For example when the user specified to exit the game or the user won the game.
     */
    protected boolean shouldStop() {
        return isExitSpecified || this.state.isWin();
    }

    /**
     * @param action The action received from the user.
     * @return The result of the action.
     */
    protected ActionResult processAction(@NotNull Action action) {
        if (action instanceof InvalidInput i) {
            return new ActionResult.Failed(action, i.getMessage());
        }

        if (action instanceof Undo) {
            final var shouldUndo = this.state.getUndoQuota()
                    .map(it -> it > 0)
                    .orElse(true);
            if (shouldUndo) {
                this.state.undo();
                return new ActionResult.Success(action);
            } else {
                return new ActionResult.Failed(action, UNDO_QUOTA_RUN_OUT);
            }
        }

        if (action instanceof Exit) {
            this.isExitSpecified = true;
            return new ActionResult.Success(action);
        }

        if (action instanceof Move move) {
            final var currentPlayerPos = this.state.getPlayerPositionById(action.getInitiator());
            if (currentPlayerPos == null) {
                return new ActionResult.Failed(action, PLAYER_NOT_FOUND);
            }
            return this.processOneStepMove(currentPlayerPos, move);
        }
        return null;
    }

    /**
     * @param playerPosition The position of the player.
     * @param move           One-step move. We assume every move has only one step.
     * @return The action result for the move.
     */
    @NotNull
    private ActionResult processOneStepMove(@NotNull Position playerPosition, @NotNull Move move) {
        final var nextPlayerPos = move.nextPosition(playerPosition);
        final var nextEntity = this.state.getEntity(nextPlayerPos);

        if (nextEntity instanceof Empty) {
            this.state.move(playerPosition, nextPlayerPos); // move if next place is empty
            return new ActionResult.Success(move);
        }

        if (nextEntity instanceof Wall) {
            return new ActionResult.Failed(move, "You hit a wall.");
        }

        if (nextEntity instanceof Player) {
            return new ActionResult.Failed(move, "You hit another player.");
        }

        if (nextEntity instanceof Box box) {
            if (box.getPlayerId() != move.getInitiator()) {
                return new ActionResult.Failed(move, "You cannot move other players' boxes.");
            }
            final var nextBoxPos = move.nextPosition(nextPlayerPos);
            if (!(this.state.getEntity(nextBoxPos) instanceof Empty))
                return new ActionResult.Failed(move, "Failed to push the box.");
            this.state.move(nextPlayerPos, nextBoxPos);
            this.state.move(playerPosition, nextPlayerPos);
            // Game history checkpoint reached if any box is moved.
            this.state.checkpoint();
            return new ActionResult.Success(move);
        }
        throw new ShouldNotReachException();
    }
}
