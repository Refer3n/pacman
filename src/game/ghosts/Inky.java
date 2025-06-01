package game.ghosts;

import board.Board;
import java.awt.Color;

/**
 * Inky - The cyan ghost that directly chases Pacman but moves slower
 */
public class Inky extends Ghost {

    private int lastPlayerRow = 0;
    private int lastPlayerCol = 0;
    private long lastPlayerPositionUpdateTime = 0;
    private static final long PLAYER_POSITION_UPDATE_DELAY = 300;

    public Inky(Board board, int startRow, int startCol) {
        super(board, startRow, startCol, "inky", new Color(0, 255, 255), 
              board.getHeight() - 1, 0);

        speed = BASE_GHOST_SPEED * 0.6f;
    }

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
        // Inky leaves home after 15 seconds (longer delay than others)
        return (currentTime - stateChangeTime) > 7000;
    }
}
