package game.ghosts;

import board.Board;
import java.awt.Color;

public class Blinky extends Ghost {

    private int lastPlayerRow = 0;
    private int lastPlayerCol = 0;
    private long lastPlayerPositionUpdateTime = 0;
    private static final long PLAYER_POSITION_UPDATE_DELAY = 300;

    public Blinky(Board board, int startRow, int startCol) {
        super(board, startRow, startCol, "blinky", Color.RED, 0, board.getWidth() - 1);

        speed = BASE_GHOST_SPEED * 1.05f;
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
        return true;
    }
}
