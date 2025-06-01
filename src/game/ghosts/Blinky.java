package game.ghosts;

import board.Board;
import java.awt.Color;

/**
 * Blinky - The red ghost that directly chases Pacman
 */
public class Blinky extends Ghost {

    private int lastPlayerRow = 0;
    private int lastPlayerCol = 0;
    private long lastPlayerPositionUpdateTime = 0;
    private static final long PLAYER_POSITION_UPDATE_DELAY = 300;

    public Blinky(Board board, int startRow, int startCol) {
        super(board, startRow, startCol, "blinky", Color.RED, 0, board.getWidth() - 1);

        // Blinky is slightly faster than other ghosts
        speed = BASE_GHOST_SPEED * 1.05f;
    }

    @Override
    protected void chase(float deltaTime, int playerRow, int playerCol) {
        long currentTime = System.currentTimeMillis();

        // Update player position periodically to create more natural pursuit
        if (currentTime - lastPlayerPositionUpdateTime > PLAYER_POSITION_UPDATE_DELAY) {
            lastPlayerRow = playerRow;
            lastPlayerCol = playerCol;
            lastPlayerPositionUpdateTime = currentTime;
        }

        // Blinky directly targets Pacman's cached position
        decideNextDirection(lastPlayerRow, lastPlayerCol);
        moveInDirection(deltaTime);
    }

    @Override
    protected boolean shouldLeaveHome(long currentTime) {
        // Blinky starts outside the ghost house
        return true;
    }
}
