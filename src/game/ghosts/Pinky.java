package game.ghosts;

import board.Board;
import java.awt.Color;

public class Pinky extends Ghost {

    private static final int DETECTION_RADIUS = 6;

    private int lastPlayerRow = 0;
    private int lastPlayerCol = 0;
    private long lastPlayerPositionUpdateTime = 0;
    private static final long PLAYER_POSITION_UPDATE_DELAY = 400;

    private long lastRandomDirectionChange = 0;
    private static final long RANDOM_DIRECTION_DELAY = 800;

    public Pinky(Board board, int startRow, int startCol) {
        super(board, startRow, startCol, "pinky", new Color(255, 192, 203), 0, 0);

        speed = BASE_GHOST_SPEED * 0.95f;
    }
    
    @Override
    protected void chase(float deltaTime, int playerRow, int playerCol) {
        long currentTime = System.currentTimeMillis();

        double distanceToPlayer = Math.sqrt(
            Math.pow(row - playerRow, 2) + 
            Math.pow(col - playerCol, 2)
        );

        if (currentTime - lastPlayerPositionUpdateTime > PLAYER_POSITION_UPDATE_DELAY) {
            lastPlayerRow = playerRow;
            lastPlayerCol = playerCol;
            lastPlayerPositionUpdateTime = currentTime;
        }

        if (distanceToPlayer <= DETECTION_RADIUS) {
            decideNextDirection(lastPlayerRow, lastPlayerCol);
        }
        else {
            if (isAtIntersection() || !canMove(direction)) {
                if (currentTime - lastRandomDirectionChange > RANDOM_DIRECTION_DELAY) {
                    int[] availableDirections = getAvailableDirections();
                    if (availableDirections.length > 0) {
                        int[] filteredDirections = availableDirections;
                        if (availableDirections.length > 1) {
                            filteredDirections = filterOppositeDirection(availableDirections);
                        }
                        
                        if (filteredDirections.length > 0) {
                            nextDirection = filteredDirections[random.nextInt(filteredDirections.length)];
                            lastRandomDirectionChange = currentTime;
                        }
                    }
                }
            }
        }
        
        moveInDirection(deltaTime);
    }

    private int[] filterOppositeDirection(int[] availableDirections) {
        int[] result = new int[availableDirections.length];
        int count = 0;
        
        for (int dir : availableDirections) {
            if (!isOppositeDirection(dir, direction)) {
                result[count++] = dir;
            }
        }

        int[] filtered = new int[count];
        System.arraycopy(result, 0, filtered, 0, count);
        
        return filtered;
    }

    @Override
    protected boolean shouldLeaveHome(long currentTime) {
        return (currentTime - stateChangeTime) > 5000;
    }
}
