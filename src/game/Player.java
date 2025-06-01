package game;

import board.Board;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;


public class Player {
    public static final int RIGHT = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int UP = 3;

    private int row;
    private int col;

    private float pixelX;
    private float pixelY;

    private int direction = RIGHT;
    private int nextDirection = RIGHT;
    private int currentFrame = 0;

    private BufferedImage[][] directionFrames;
    private BufferedImage closedMouthFrame;
    private ImageIcon[] animationIcons;

    private static final float PLAYER_SPEED = 160.0f;

    private final Board board;
    private GamePanel gamePanel;

    private static final int CELL_SIZE = GamePanel.CELL_SIZE;

    private long lastMoveTime;
    private long lastInputTime = 0;
    private static final long INPUT_DEBOUNCE_TIME = 100;

    private static final float ALIGNMENT_TOLERANCE = 6.0f;

    private boolean isMoving = false;

    // Power-up related fields
    private float speedMultiplier = 1.0f;
    private int scoreMultiplier = 1;
    private int lives = 3;
    private boolean ghostKillerMode = false;
    
    public Player(Board board, int startRow, int startCol) {
        this.board = board;
        this.row = startRow;
        this.col = startCol;

        this.pixelX = col * CELL_SIZE;
        this.pixelY = row * CELL_SIZE;

        this.lastMoveTime = System.currentTimeMillis();

        loadAnimationFrames();
        prepareAnimationIcons();
    }

    private void loadAnimationFrames() {
        try {
            directionFrames = new BufferedImage[4][2];

            directionFrames[RIGHT][0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/right_half.png")));
            directionFrames[RIGHT][1] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/right_full.png")));

            directionFrames[DOWN][0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/down_half.png")));
            directionFrames[DOWN][1] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/down_full.png")));

            directionFrames[LEFT][0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/left_half.png")));
            directionFrames[LEFT][1] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/left_full.png")));

            directionFrames[UP][0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/up_half.png")));
            directionFrames[UP][1] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/up_full.png")));

            closedMouthFrame = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/closed.png")));

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        prepareAnimationIcons();
    }

    private void prepareAnimationIcons() {
        animationIcons = new ImageIcon[3];
        updateDirectionIcons();
    }

    private void updateDirectionIcons() {
        if (directionFrames != null && closedMouthFrame != null) {
            int size = GamePanel.CELL_SIZE;

            if (gamePanel != null) {
                float cellWidth = (float) gamePanel.getWidth() / board.getWidth();
                float cellHeight = (float) gamePanel.getHeight() / board.getHeight();

                size = Math.min((int)cellWidth, (int)cellHeight);
            }
    
            Image halfOpen = directionFrames[direction][0].getScaledInstance(size, size, Image.SCALE_SMOOTH);
            Image fullOpen = directionFrames[direction][1].getScaledInstance(size, size, Image.SCALE_SMOOTH);
            Image closed = closedMouthFrame.getScaledInstance(size, size, Image.SCALE_SMOOTH);

            animationIcons[0] = new ImageIcon(halfOpen);
            animationIcons[1] = new ImageIcon(fullOpen);
            animationIcons[2] = new ImageIcon(closed);
        }
    }


    public void update(long currentTime) {
        float deltaTime = (currentTime - lastMoveTime) / 1000.0f;
        lastMoveTime = currentTime;

        if (deltaTime > 0.1f) {
            deltaTime = 0.1f;
        }

        float centerX = col * CELL_SIZE;
        float centerY = row * CELL_SIZE;

        boolean isAlignedX = Math.abs(pixelX - centerX) < ALIGNMENT_TOLERANCE;
        boolean isAlignedY = Math.abs(pixelY - centerY) < ALIGNMENT_TOLERANCE;

        if (nextDirection != direction) {
            boolean isOppositeDirection =
                    (direction == RIGHT && nextDirection == LEFT) ||
                            (direction == LEFT && nextDirection == RIGHT) ||
                            (direction == UP && nextDirection == DOWN) ||
                            (direction == DOWN && nextDirection == UP);

            if (isOppositeDirection) {
                direction = nextDirection;
                updateDirectionIcons();
            }
            else if ((isAlignedX && (nextDirection == UP || nextDirection == DOWN)) ||
                    (isAlignedY && (nextDirection == LEFT || nextDirection == RIGHT))) {

                if (canMove(nextDirection)) {
                    if (nextDirection == UP || nextDirection == DOWN) {
                        pixelX = centerX;
                    } else {
                        pixelY = centerY;
                    }

                    direction = nextDirection;
                    updateDirectionIcons();
                }
            }
        }

        if (canMove(direction)) {
            isMoving = true;
            move(direction, deltaTime);
        } else {
            isMoving = false;
            switch (direction) {
                case RIGHT:
                    pixelX = (col * CELL_SIZE) - 1;
                    break;
                case DOWN:
                    pixelY = (row * CELL_SIZE) - 1;
                    break;
                case LEFT:
                    pixelX = (col * CELL_SIZE) + 1;
                    break;
                case UP:
                    pixelY = (row * CELL_SIZE) + 1;
                    break;
            }
        }
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void setAnimationFrame(int frame) {
        if (frame >= 0 && frame < 3) {
            this.currentFrame = frame;
        }
    }

    public ImageIcon getCurrentIcon() {
        if (animationIcons != null && currentFrame >= 0 && currentFrame < animationIcons.length) {
            return animationIcons[currentFrame];
        }
        return null;
    }

    public void setNextDirection(int direction) {
        if (direction >= 0 && direction <= 3) {
            long currentTime = System.currentTimeMillis();

            if (direction != nextDirection && currentTime - lastInputTime > INPUT_DEBOUNCE_TIME) {
                this.nextDirection = direction;
                lastInputTime = currentTime;
            }
        }
    }

    private boolean canMove(int direction) {
        int newRow = row;
        int newCol = col;

        switch (direction) {
            case RIGHT:
                newCol++;
                break;
            case DOWN:
                newRow++;
                break;
            case LEFT:
                newCol--;
                break;
            case UP:
                newRow--;
                break;
        }

        if (newRow < 0 || newRow >= board.getHeight() || newCol < 0 || newCol >= board.getWidth()) {
            return false;
        }

        char tile = board.getTile(newRow, newCol);
        return tile != '|' && tile != 'X';
    }

    private void move(int direction, float deltaTime) {
        // Apply speed multiplier from power-ups
        float distance = PLAYER_SPEED * speedMultiplier * deltaTime;

        float oldPixelX = pixelX;
        float oldPixelY = pixelY;

        switch (direction) {
            case RIGHT:
                pixelX += distance;
                break;
            case DOWN:
                pixelY += distance;
                break;
            case LEFT:
                pixelX -= distance;
                break;
            case UP:
                pixelY -= distance;
                break;
        }

        int newRow = Math.round(pixelY / CELL_SIZE);
        int newCol = Math.round(pixelX / CELL_SIZE);

        if (newRow != row || newCol != col) {
            if (canMove(direction)) {
                row = newRow;
                col = newCol;

                char currentTile = board.getTile(row, col);
                if (currentTile == '.') {
                    board.updateTile(row, col, ' ');

                    if (gamePanel != null) {
                        gamePanel.clearDot(row, col);
                        
                        // Award points for eating dot
                        int basePoints = 10;
                        int points = basePoints * scoreMultiplier;
                        
                        // Update score in game window via game panel
                        if (gamePanel.getParent() instanceof JPanel) {
                            Component parent = gamePanel.getParent();
                            while (parent != null) {
                                if (parent instanceof GameWindow) {
                                    ((GameWindow) parent).updateScore(points);
                                    break;
                                }
                                parent = parent.getParent();
                            }
                        }
                    }
                }
            } else {
                pixelX = oldPixelX;
                pixelY = oldPixelY;

                switch (direction) {
                    case RIGHT:
                        pixelX = col * CELL_SIZE + CELL_SIZE - 1;
                        break;
                    case DOWN:
                        pixelY = row * CELL_SIZE + CELL_SIZE - 1;
                        break;
                    case LEFT:
                        pixelX = col * CELL_SIZE;
                        break;
                    case UP:
                        pixelY = row * CELL_SIZE;
                        break;
                }

                isMoving = false;
            }
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }


    public float getPixelX() {
        if (gamePanel != null) {
            float cellWidthRatio = (float) gamePanel.getWidth() / (board.getWidth() * CELL_SIZE);
            return pixelX * cellWidthRatio;
        }
        return pixelX;
    }

    public float getPixelY() {
        if (gamePanel != null) {
            float cellHeightRatio = (float) gamePanel.getHeight() / (board.getHeight() * CELL_SIZE);
            return pixelY * cellHeightRatio;
        }
        return pixelY;
    }

    public int getDirection() {
        return direction;
    }

    public void reset(int startRow, int startCol) {
        this.row = startRow;
        this.col = startCol;
        this.pixelX = col * CELL_SIZE;
        this.pixelY = row * CELL_SIZE;
        this.direction = RIGHT;
        this.nextDirection = RIGHT;
        this.isMoving = false;

        long currentTime = System.currentTimeMillis();
        this.lastMoveTime = currentTime;
        this.lastInputTime = currentTime;

        updateDirectionIcons();
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void updateSprite() {
        updateDirectionIcons();
    }
    
    /**
     * Sets the speed multiplier for power-ups
     */
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }
    
    /**
     * Gets the current speed multiplier
     */
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }
    
    /**
     * Sets the score multiplier for power-ups
     */
    public void setScoreMultiplier(int multiplier) {
        this.scoreMultiplier = multiplier;
    }
    
    /**
     * Gets the current score multiplier
     */
    public int getScoreMultiplier() {
        return scoreMultiplier;
    }
    
    /**
     * Adds an extra life, up to a maximum of 3
     */
    public void addLife() {
        if (this.lives < 3) {
            this.lives++;
        }
    }
    
    /**
     * Removes a life
     * @return true if the player still has lives, false if game over
     */
    public boolean loseLife() {
        this.lives--;
        return lives > 0;
    }
    
    /**
     * Gets the number of lives
     */
    public int getLives() {
        return lives;
    }
    
    /**
     * Sets ghost killer mode
     */
    public void setGhostKillerMode(boolean enabled) {
        this.ghostKillerMode = enabled;
    }
    
    /**
     * Checks if ghost killer mode is active
     */
    public boolean isGhostKillerMode() {
        return ghostKillerMode;
    }
    
    /**
     * Resets all power-up effects
     */
    public void resetPowerUps() {
        speedMultiplier = 1.0f;
        scoreMultiplier = 1;
        ghostKillerMode = false;
    }
}

