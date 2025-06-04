package game.ghosts;

import board.Board;
import java.awt.Color;

public class Inky extends Ghost {

    private int lastPlayerRow = 0;
    private int lastPlayerCol = 0;
    private long lastPlayerPositionUpdateTime = 0;

    private static final long PLAYER_POSITION_UPDATE_DELAY = 500;

    public Inky(Board board, int startRow, int startCol) {
        super(board, startRow, startCol, "inky", new Color(0, 255, 255), 
              board.getHeight() - 1, 0);

        speed = BASE_GHOST_SPEED * 0.7f;
    }
    
    @Override
    protected void chase(float deltaTime, int playerRow, int playerCol) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastPlayerPositionUpdateTime > PLAYER_POSITION_UPDATE_DELAY) {
            lastPlayerRow = playerRow;
            lastPlayerCol = playerCol;
            lastPlayerPositionUpdateTime = currentTime;
        }

        decideNextDirection(lastPlayerRow, lastPlayerCol);
        moveInDirection(deltaTime);
    }
    
    @Override
    protected boolean shouldLeaveHome(long currentTime) {
        return (currentTime - stateChangeTime) > 15000;
    }
}
