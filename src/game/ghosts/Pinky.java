package game.ghosts;

import board.Board;
import java.awt.Color;

/**
 * Pinky - The pink ghost with a detection/chase behavior
 * Moves randomly until it detects the player, then chases directly
 */
public class Pinky extends Ghost {
    
    // Detection radius for player (in tiles)
    private static final int DETECTION_RADIUS = 6;
    
    // Last known player position for chasing
    private int lastPlayerRow = 0;
    private int lastPlayerCol = 0;
    private long lastPlayerPositionUpdateTime = 0;
    private static final long PLAYER_POSITION_UPDATE_DELAY = 400; // milliseconds
    
    // Direction change at intersections
    private long lastRandomDirectionChange = 0;
    private static final long RANDOM_DIRECTION_DELAY = 800; // milliseconds
    
    /**
     * Creates a new Pinky ghost
     * 
     * @param board Game board
     * @param startRow Starting row
     * @param startCol Starting column
     */
    public Pinky(Board board, int startRow, int startCol) {
        super(board, startRow, startCol, "pinky", new Color(255, 192, 203), 0, 0);
        
        // Pinky is slightly slower than Blinky
        speed = BASE_GHOST_SPEED * 0.95f;
    }
    
    @Override
    protected void chase(float deltaTime, int playerRow, int playerCol) {
        long currentTime = System.currentTimeMillis();
        
        // Calculate distance to player
        double distanceToPlayer = Math.sqrt(
            Math.pow(row - playerRow, 2) + 
            Math.pow(col - playerCol, 2)
        );
        
        // Update player position periodically
        if (currentTime - lastPlayerPositionUpdateTime > PLAYER_POSITION_UPDATE_DELAY) {
            lastPlayerRow = playerRow;
            lastPlayerCol = playerCol;
            lastPlayerPositionUpdateTime = currentTime;
        }
        
        // If player is within detection radius, chase directly (like Blinky)
        if (distanceToPlayer <= DETECTION_RADIUS) {
            // Direct chase mode - use Blinky's approach
            decideNextDirection(lastPlayerRow, lastPlayerCol);
        } 
        // Otherwise, move randomly
        else {
            // Only change direction at intersections or when facing a wall
            if (isAtIntersection() || !canMove(direction)) {
                // Add a delay to avoid rapid direction changes
                if (currentTime - lastRandomDirectionChange > RANDOM_DIRECTION_DELAY) {
                    int[] availableDirections = getAvailableDirections();
                    if (availableDirections.length > 0) {
                        // Avoid opposite direction unless it's the only option
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
    
    /**
     * Filters out the opposite direction from available directions
     */
    private int[] filterOppositeDirection(int[] availableDirections) {
        int[] result = new int[availableDirections.length];
        int count = 0;
        
        for (int dir : availableDirections) {
            if (!isOppositeDirection(dir, direction)) {
                result[count++] = dir;
            }
        }
        
        // Create properly sized array
        int[] filtered = new int[count];
        System.arraycopy(result, 0, filtered, 0, count);
        
        return filtered;
    }
    
    /**
     * Gets array of valid directions
     */
    private int[] getAvailableDirections() {
        int[] directions = new int[4];
        int count = 0;
        
        // Check each direction
        if (canMove(RIGHT)) directions[count++] = RIGHT;
        if (canMove(DOWN)) directions[count++] = DOWN;
        if (canMove(LEFT)) directions[count++] = LEFT;
        if (canMove(UP)) directions[count++] = UP;
        
        // Create properly sized array
        int[] result = new int[count];
        System.arraycopy(directions, 0, result, 0, count);

        return result;
    }

    @Override
    protected boolean shouldLeaveHome(long currentTime) {
        // Pinky leaves home after 5 seconds
        return (currentTime - stateChangeTime) > 5000;
    }
}
