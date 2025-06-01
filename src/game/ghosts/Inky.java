package game.ghosts;

import board.Board;
import java.awt.Color;

/**
 * Inky - The cyan ghost that directly chases Pacman but moves slower
 */
public class Inky extends Ghost {
    
    // Last known player position
    private int lastPlayerRow = 0;
    private int lastPlayerCol = 0;
    private long lastPlayerPositionUpdateTime = 0;
    
    // Slower update rate to make movement more predictable
    private static final long PLAYER_POSITION_UPDATE_DELAY = 500; // milliseconds (slower than Blinky)
    
    /**
     * Creates a new Inky ghost
     * 
     * @param board Game board
     * @param startRow Starting row
     * @param startCol Starting column
     */
    public Inky(Board board, int startRow, int startCol) {
        super(board, startRow, startCol, "inky", new Color(0, 255, 255), 
              board.getHeight() - 1, 0);
        
        // Inky is significantly slower than other ghosts
        speed = BASE_GHOST_SPEED * 0.8f; // 80% of standard ghost speed
    }
    
    @Override
    protected void chase(float deltaTime, int playerRow, int playerCol) {
        long currentTime = System.currentTimeMillis();
        
        // Update player position periodically to create more relaxed pursuit
        if (currentTime - lastPlayerPositionUpdateTime > PLAYER_POSITION_UPDATE_DELAY) {
            lastPlayerRow = playerRow;
            lastPlayerCol = playerCol;
            lastPlayerPositionUpdateTime = currentTime;
        }
        
        // Inky directly targets Pacman's cached position (like Blinky but slower)
        decideNextDirection(lastPlayerRow, lastPlayerCol);
        moveInDirection(deltaTime);
    }
    
    @Override
    protected boolean shouldLeaveHome(long currentTime) {
        // Inky leaves home after 15 seconds (longer delay than others)
        return (currentTime - stateChangeTime) > 15000;
    }
}
