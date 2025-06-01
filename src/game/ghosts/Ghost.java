package game.ghosts;

import board.Board;
import game.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;

public abstract class Ghost {
    public static final int RIGHT = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int UP = 3;

    protected static final float BASE_GHOST_SPEED = 100.0f;

    protected int row;
    protected int col;
    protected float pixelX;
    protected float pixelY;
    protected int direction = RIGHT;
    protected int nextDirection = RIGHT;
    protected int currentFrame = 0;

    protected final String ghostName;
    protected final Color ghostColor;
    protected GhostState currentState = GhostState.IN_HOME;
    protected final Board board;
    protected GamePanel gamePanel;

    protected BufferedImage[][] directionFrames;
        protected ImageIcon[] animationIcons;

    protected long lastMoveTime = 0;
    protected float speed = BASE_GHOST_SPEED;

    protected final Random random = new Random();

    protected final int scatterCornerRow;
    protected final int scatterCornerCol;

    protected int homeRow;
    protected int homeCol;
    protected int exitRow;
    protected int exitCol;
    protected long stateChangeTime;
    protected static final long SCATTER_DURATION = 7000;
    protected static final long CHASE_DURATION = 20000;

    private static final float ALIGNMENT_TOLERANCE = 4.0f;

    // For limiting direction changes
    private long lastDirectionDecisionTime = 0;
    
    // For delaying target updates
    protected static final long TARGET_UPDATE_DELAY = 500; // milliseconds
    protected long lastTargetUpdateTime = 0;
    protected int targetRow;
    protected int targetCol;
    private static final long DIRECTION_DECISION_DELAY = 500; // milliseconds


    public Ghost(Board board, int startRow, int startCol, String ghostName, Color color,
                 int scatterRow, int scatterCol) {
        this.board = board;
        this.row = startRow;
        this.col = startCol;
        this.homeRow = startRow;
        this.homeCol = startCol;
        
        // Initialize target position to scatter position
        this.targetRow = scatterRow;
        this.targetCol = scatterCol;

        this.exitRow = startRow - 2;
        this.exitCol = startCol;

        this.ghostName = ghostName;
        this.ghostColor = color;
        this.scatterCornerRow = scatterRow;
        this.scatterCornerCol = scatterCol;

        // Initialize pixel position based on grid coordinates
        this.pixelX = col * GamePanel.CELL_SIZE;
        this.pixelY = row * GamePanel.CELL_SIZE;

        this.lastMoveTime = System.currentTimeMillis();
        this.stateChangeTime = lastMoveTime + SCATTER_DURATION;

        loadAnimationFrames();
    }

    /**
     * Loads animation frames for the ghost from assets
     */
    private void loadAnimationFrames() {
        try {
            // Load direction frames (2 frames for each direction)
            directionFrames = new BufferedImage[4][2];

            // Right direction frames
            directionFrames[RIGHT][0] = ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/ghosts/" + ghostName + "/right_1.png")));
            directionFrames[RIGHT][1] = ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/ghosts/" + ghostName + "/right_2.png")));

            // Down direction frames
            directionFrames[DOWN][0] = ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/ghosts/" + ghostName + "/down_1.png")));
            directionFrames[DOWN][1] = ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/ghosts/" + ghostName + "/down_2.png")));

            // Left direction frames
            directionFrames[LEFT][0] = ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/ghosts/" + ghostName + "/left_1.png")));
            directionFrames[LEFT][1] = ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/ghosts/" + ghostName + "/left_2.png")));

            // Up direction frames
            directionFrames[UP][0] = ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/ghosts/" + ghostName + "/up_1.png")));
            directionFrames[UP][1] = ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/ghosts/" + ghostName + "/up_2.png")));

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        // Initialize animation icons
        prepareAnimationIcons();
    }

    /**
     * Creates image icons from the loaded frames
     */
    private void prepareAnimationIcons() {
        int size = GamePanel.CELL_SIZE;

        // Normal direction icons
        animationIcons = new ImageIcon[2];
        updateDirectionIcons();
    }

    /**
     * Updates the animation icons based on current direction
     */
    protected void updateDirectionIcons() {
        if (directionFrames != null) {
            int size = GamePanel.CELL_SIZE;

            Image frame1 = directionFrames[direction][0].getScaledInstance(size, size, Image.SCALE_SMOOTH);
            Image frame2 = directionFrames[direction][1].getScaledInstance(size, size, Image.SCALE_SMOOTH);

            animationIcons[0] = new ImageIcon(frame1);
            animationIcons[1] = new ImageIcon(frame2);
        }
    }

    public void update(long currentTime, int playerRow, int playerCol) {
        float deltaTime = (currentTime - lastMoveTime) / 1000.0f;
        lastMoveTime = currentTime;

        // Cap deltaTime to avoid large jumps
        if (deltaTime > 0.1f) {
            deltaTime = 0.1f;
        }

        // Check for state changes
        checkStateTransition(currentTime);

        // Handle movement based on current state
        switch (currentState) {
            case IN_HOME:
                moveInHome(deltaTime);
                break;

            case LEAVING_HOME:
                // Navigate to the exit point
                leavingHome(deltaTime);
                break;

            case CHASE:
                // Chase Pacman (specific to each ghost type)
                chase(deltaTime, playerRow, playerCol);
                break;

            case SCATTER:
                // Move to scatter corner
                moveToScatterPosition(deltaTime);
                break;

            case FRIGHTENED:
                // Random frightened movement
                moveFrightened(deltaTime);
                break;
        }
    
        // Ensure proper grid alignment when needed
        ensureGridAlignment();
    }
    
    /**
     * Ensures the ghost stays properly aligned with the grid
     */
    private void ensureGridAlignment() {
        // Calculate current cell center
        float centerX = col * GamePanel.CELL_SIZE;
        float centerY = row * GamePanel.CELL_SIZE;
        
        // When moving horizontally, ensure vertical alignment
        if (direction == LEFT || direction == RIGHT) {
            // Only align Y when close to center to avoid bumps
            if (Math.abs(pixelY - centerY) < ALIGNMENT_TOLERANCE / 2) {
                pixelY = centerY;
            }
        }
        
        // When moving vertically, ensure horizontal alignment
        if (direction == UP || direction == DOWN) {
            // Only align X when close to center to avoid bumps
            if (Math.abs(pixelX - centerX) < ALIGNMENT_TOLERANCE / 2) {
                pixelX = centerX;
            }
        }
    }

    /**
     * Checks if it's time to transition to a different state
     */
    private void checkStateTransition(long currentTime) {
        // We're skipping SCATTER to CHASE transitions
        // Only transitioning from SCATTER to CHASE if in SCATTER mode
        if (currentState == GhostState.SCATTER && currentTime > stateChangeTime) {
            currentState = GhostState.CHASE;
            // Reverse direction when changing states
            reverseDirection();
        }
        // No transition from CHASE to SCATTER - ghosts will stay in chase mode permanently

        // Logic for releasing ghosts from the ghost house
        if (currentState == GhostState.IN_HOME && shouldLeaveHome(currentTime)) {
            currentState = GhostState.LEAVING_HOME;
        }
    }

    /**
     * Reverses the ghost's direction
     */
    private void reverseDirection() {
        switch (direction) {
            case RIGHT: direction = LEFT; break;
            case LEFT: direction = RIGHT; break;
            case UP: direction = DOWN; break;
            case DOWN: direction = UP; break;
        }
        nextDirection = direction;
        
        // Reset target position
        targetRow = scatterCornerRow;
        targetCol = scatterCornerCol;
        lastTargetUpdateTime = System.currentTimeMillis();
    }

    /**
     * Determines if the ghost should leave home
     * Override in subclasses for different release timing
     */
    protected boolean shouldLeaveHome(long currentTime) {
        // Basic implementation: leave after a few seconds
        return (currentTime - stateChangeTime) > 3000;
    }

    /**
     * Moves the ghost inside the ghost house
     */
    private void moveInHome(float deltaTime) {
        // Simple up and down movement inside the ghost house
        float moveDistance = speed * 0.5f * deltaTime; // Slower inside home

        if (direction == UP) {
            pixelY -= moveDistance;
            if (pixelY < (homeRow - 0.5) * GamePanel.CELL_SIZE) {
                direction = DOWN;
            }
        } else {
            pixelY += moveDistance;
            if (pixelY > (homeRow + 0.5) * GamePanel.CELL_SIZE) {
                direction = UP;
            }
        }
    }

    /**
     * Moves the ghost out of the ghost house
     */
    private void leavingHome(float deltaTime) {
        // Use a fixed slower speed when leaving home
        float moveDistance = speed * 0.7f * deltaTime;
    
        // Calculate position to the exit
        float exitPixelX = exitCol * GamePanel.CELL_SIZE;
        float exitPixelY = exitRow * GamePanel.CELL_SIZE;
    
        // First move vertically to the exit row
        if (Math.abs(pixelY - exitPixelY) > ALIGNMENT_TOLERANCE) {
            // Move up or down to exit row
            direction = (pixelY > exitPixelY) ? UP : DOWN;
            pixelY += (direction == UP) ? -moveDistance : moveDistance;
            
            // Keep X centered on exit column if we're close
            if (Math.abs(pixelX - exitPixelX) < ALIGNMENT_TOLERANCE * 2) {
                pixelX = exitPixelX;
            }
        }
        // Then align with the exit column horizontally
        else if (Math.abs(pixelX - exitPixelX) > ALIGNMENT_TOLERANCE) {
            // Ensure we're at the exit row height
            pixelY = exitPixelY;
            
            // Move horizontally toward exit column
            direction = (pixelX > exitPixelX) ? LEFT : RIGHT;
            pixelX += (direction == LEFT) ? -moveDistance : moveDistance;
        }
        // Once at the exit point, start normal movement
        else {
            // Snap to exact exit position
            pixelX = exitPixelX;
            pixelY = exitPixelY;
            row = exitRow;
            col = exitCol;
    
            // Set direction to UP (typical direction when leaving home)
            direction = UP;
            nextDirection = UP;
            updateDirectionIcons();
    
            // Transition to chase mode
            currentState = GhostState.CHASE;
        }
    }

    /**
     * Abstract method for chase behavior - implemented by each ghost type
     */
    protected abstract void chase(float deltaTime, int playerRow, int playerCol);

    /**
     * Moves the ghost toward its scatter corner
     */
    private void moveToScatterPosition(float deltaTime) {
        // Calculate next position based on scatter target
        decideNextDirection(scatterCornerRow, scatterCornerCol);

        // Move in the chosen direction
        moveInDirection(deltaTime);
    }

    /**
     * Random movement when frightened
     */
    private void moveFrightened(float deltaTime) {
        // Slow down when frightened
        float frightenedSpeed = speed * 0.5f;

        // At intersections, choose a random valid direction (excluding reversal)
        if (isAtIntersection()) {
            // Get available directions excluding the opposite of current direction
            int[] availableDirections = getAvailableDirections();
            if (availableDirections.length > 0) {
                nextDirection = availableDirections[random.nextInt(availableDirections.length)];
            }
        }

        // Move at reduced speed
        float moveDistance = frightenedSpeed * deltaTime;
        moveInDirection(deltaTime);
    }


    /**
     * Decides the next direction based on target position
     * Always takes the shortest path to the target
     */
    protected void decideNextDirection(int targetRow, int targetCol) {
        // Make a decision at intersections or when blocked
        if (isAtIntersection() || !canMove(direction)) {
            // Calculate distances for each possible direction
            int bestDirection = direction;
            double bestDistance = Double.MAX_VALUE;

            // Get available directions
            int[] availableDirections = getAvailableDirections();

            // If we only have one option, take it
            if (availableDirections.length == 1) {
                nextDirection = availableDirections[0];
                return;
            }

            // Find which direction gets us closest to the target
            for (int dir : availableDirections) {
                // Skip opposite direction (no U-turns) unless it's the only option
                if (isOppositeDirection(dir, direction) && availableDirections.length > 1) {
                    continue;
                }

                // Calculate where we'd end up
                int newRow = row;
                int newCol = col;

                switch (dir) {
                    case RIGHT: newCol++; break;
                    case DOWN: newRow++; break;
                    case LEFT: newCol--; break;
                    case UP: newRow--; break;
                }

                // Calculate exact distance to target (no randomness)
                double distance = Math.sqrt(
                    Math.pow(newRow - targetRow, 2) +
                    Math.pow(newCol - targetCol, 2)
                );

                // Update best direction if this is better
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestDirection = dir;
                }
            }

            // Update the next direction
            nextDirection = bestDirection;
        } else {
            // In corridors, continue in the same direction
            nextDirection = direction;
        }
    }

    /**
     * Moves the ghost in the current direction
     */
    protected void moveInDirection(float deltaTime) {
        // Calculate movement distance
        float moveDistance = speed * deltaTime;
        
        // Calculate current cell center
        float centerX = col * GamePanel.CELL_SIZE;
        float centerY = row * GamePanel.CELL_SIZE;
        
        // Check alignment with current cell center
        boolean isAlignedX = Math.abs(pixelX - centerX) < ALIGNMENT_TOLERANCE;
        boolean isAlignedY = Math.abs(pixelY - centerY) < ALIGNMENT_TOLERANCE;
        
        // Handle direction changes at intersections or when aligned with grid
        if ((isAlignedX && isAlignedY) && nextDirection != direction) {
            // We're at a cell center - check if we can change direction
            boolean canChangeToNext = canMove(nextDirection);
            boolean isOpposite = isOppositeDirection(direction, nextDirection);
            
            // Allow direction change if we can move that way or it's the opposite direction
            if (canChangeToNext || isOpposite) {
                // Perfectly align with cell center when changing direction
                pixelX = centerX;
                pixelY = centerY;
                
                direction = nextDirection;
                updateDirectionIcons();
            }
        }
        
        // Check if we can move in the current direction
        if (canMove(direction)) {
            // Store old position
            float oldPixelX = pixelX;
            float oldPixelY = pixelY;
            
            // Move in the current direction
            switch (direction) {
                case RIGHT: pixelX += moveDistance; break;
                case DOWN: pixelY += moveDistance; break;
                case LEFT: pixelX -= moveDistance; break;
                case UP: pixelY -= moveDistance; break;
            }
            
            // Calculate new grid position
            int newRow = Math.round(pixelY / GamePanel.CELL_SIZE);
            int newCol = Math.round(pixelX / GamePanel.CELL_SIZE);
            
            // If we're moving to a new cell
            if (newRow != row || newCol != col) {
                // Verify we can move to the new cell
                if (canMove(direction)) {
                    // Update grid position
                    row = newRow;
                    col = newCol;
                    
                    // Check for decision points if we're near the center of the new cell
                    float newCenterX = newCol * GamePanel.CELL_SIZE;
                    float newCenterY = newRow * GamePanel.CELL_SIZE;
                    
                    // If we're close to the new cell center and at an intersection, make a decision
                    if (Math.abs(pixelX - newCenterX) < ALIGNMENT_TOLERANCE && 
                        Math.abs(pixelY - newCenterY) < ALIGNMENT_TOLERANCE && 
                        isAtIntersection()) {
                        // This will be handled by the specific ghost behavior
                    }
                } else {
                    // Can't move to the new cell - revert and adjust position
                    pixelX = oldPixelX;
                    pixelY = oldPixelY;
                    
                    // Choose a new direction
                    int[] availableDirections = getAvailableDirections();
                    if (availableDirections.length > 0) {
                        nextDirection = availableDirections[random.nextInt(availableDirections.length)];
                    }
                }
            }
        } else {
            // We're facing a wall - select a new direction
            int[] availableDirections = getAvailableDirections();
            if (availableDirections.length > 0) {
                nextDirection = availableDirections[random.nextInt(availableDirections.length)];
                
                // Immediately change direction if possible
                direction = nextDirection;
                updateDirectionIcons();
                
                // Align with grid to ensure clean movement
                if (direction == UP || direction == DOWN) {
                    pixelX = centerX;
                } else {
                    pixelY = centerY;
                }
            }
        }
    }

    /**
     * Checks if the ghost can change direction at the current position
     */
    private boolean canChangeDirection() {
        // If next direction is opposite, can always change
        if (isOppositeDirection(direction, nextDirection)) {
            return true;
        }

        // For other directions, need to be aligned with grid
        float centerX = col * GamePanel.CELL_SIZE;
        float centerY = row * GamePanel.CELL_SIZE;

        boolean isAlignedX = Math.abs(pixelX - centerX) < ALIGNMENT_TOLERANCE;
        boolean isAlignedY = Math.abs(pixelY - centerY) < ALIGNMENT_TOLERANCE;

        // Can change if aligned and the new direction is valid
        if ((isAlignedX && (nextDirection == UP || nextDirection == DOWN)) ||
            (isAlignedY && (nextDirection == LEFT || nextDirection == RIGHT))) {
            return canMove(nextDirection);
        }

        return false;
    }

    /**
     * Gets an array of valid directions the ghost can move
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

    /**
     * Checks if the ghost is at an intersection (multiple movement options)
     */
    protected boolean isAtIntersection() {
        // Are we well-aligned with the grid?
        float centerX = col * GamePanel.CELL_SIZE;
        float centerY = row * GamePanel.CELL_SIZE;

        boolean isAligned = Math.abs(pixelX - centerX) < ALIGNMENT_TOLERANCE &&
                            Math.abs(pixelY - centerY) < ALIGNMENT_TOLERANCE;

        if (!isAligned) {
            return false;
        }

        // Count available directions
        int[] availableDirections = getAvailableDirections();
        return availableDirections.length > 2; // More than 2 options = intersection
    }

    /**
     * Checks if the ghost can move in the specified direction
     */
    protected boolean canMove(int direction) {
        // When in home or leaving home state, movement is handled specially
        if (currentState == GhostState.IN_HOME) {
            // In home, can only move up and down within a limited range
            if (direction == UP) {
                return pixelY > (homeRow - 0.5) * GamePanel.CELL_SIZE;
            } else if (direction == DOWN) {
                return pixelY < (homeRow + 0.5) * GamePanel.CELL_SIZE;
            }
            return false;
        } 
        else if (currentState == GhostState.LEAVING_HOME) {
            // When leaving home, handle special movement to exit
            float exitPixelX = exitCol * GamePanel.CELL_SIZE;
            float exitPixelY = exitRow * GamePanel.CELL_SIZE;
            
            // Allow movement toward exit
            if ((direction == UP && row >= exitRow) ||
                (direction == LEFT && col > exitCol) ||
                (direction == RIGHT && col < exitCol)) {
                return true;
            }
        }
        
        // Standard movement checks for normal gameplay
        int newRow = row;
        int newCol = col;

        switch (direction) {
            case RIGHT: newCol++; break;
            case DOWN: newRow++; break;
            case LEFT: newCol--; break;
            case UP: newRow--; break;
        }

        // Check board boundaries
        if (newRow < 0 || newRow >= board.getHeight() || newCol < 0 || newCol >= board.getWidth()) {
            return false;
        }

        // Check for walls
        char tile = board.getTile(newRow, newCol);
        return tile != '|' && tile != 'X';
    }

    /**
     * Checks if two directions are opposite
     */
    protected boolean isOppositeDirection(int dir1, int dir2) {
        return (dir1 == RIGHT && dir2 == LEFT) ||
               (dir1 == LEFT && dir2 == RIGHT) ||
               (dir1 == UP && dir2 == DOWN) ||
               (dir1 == DOWN && dir2 == UP);
    }

    /**
     * Changes the ghost state to frightened
     */
    public void frighten() {
        if (currentState != GhostState.IN_HOME && currentState != GhostState.LEAVING_HOME) {
            currentState = GhostState.FRIGHTENED;
            reverseDirection();
        }
    }

    /**
     * Sets the current animation frame
     */
    public void setAnimationFrame(int frame) {
        if (frame >= 0 && frame < 2) {
            this.currentFrame = frame;
        }
    }

    /**
     * Gets the current icon to display
     */
    public ImageIcon getCurrentIcon() {
            // Always use regular direction icons, even in frightened state
            if (animationIcons != null && currentFrame >= 0 && currentFrame < animationIcons.length) {
                return animationIcons[currentFrame];
        }
        return null;
    }

    // Getter methods
    public int getRow() { return row; }
    public int getCol() { return col; }
    public float getPixelX() { return pixelX; }
    public float getPixelY() { return pixelY; }
    public GhostState getCurrentState() { return currentState; }

    /**
     * Resets the ghost to its starting position
     */
    public void reset() {
        row = homeRow;
        col = homeCol;
        pixelX = col * GamePanel.CELL_SIZE;
        pixelY = row * GamePanel.CELL_SIZE;
        direction = RIGHT;
        nextDirection = RIGHT;
        currentState = GhostState.IN_HOME;
        stateChangeTime = System.currentTimeMillis() + 3000; // Start leaving after 3 seconds
        updateDirectionIcons();
    }

    /**
     * Sets the reference to the game panel
     */
    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
}
