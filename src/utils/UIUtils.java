package utils;

import javax.swing.*;
import java.awt.*;


public class UIUtils {
    public static JButton createStyledButton(String text, int fontSize) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        btn.setBackground(Color.BLACK);
        btn.setForeground(new Color(255, 200, 0));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));

        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(30, 30, 30));
                btn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.BLACK);
                btn.repaint();
            }
        });

        return btn;
    }
}


//package game;
//
//import board.Board;
//import javax.swing.*;
//        import java.awt.*;
//        import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.util.Objects;
//import javax.imageio.ImageIO;

///**
// * Player class for Pacman using Swing components with smoother movement
// */
//public class Player {
//    public static final int RIGHT = 0;
//    public static final int DOWN = 1;
//    public static final int LEFT = 2;
//    public static final int UP = 3;
//
//    // Tile-based position (grid coordinates)
//    private int row;
//    private int col;
//
//    // Pixel-based position (for smooth transitions)
//    private float pixelX;
//    private float pixelY;
//
//    private int direction = RIGHT;
//    private int nextDirection = RIGHT;
//    private int currentFrame = 0;
//
//    private BufferedImage[][] directionFrames;
//    private BufferedImage closedMouthFrame;
//    private ImageIcon[] animationIcons;
//
//    // Speed in pixels per second
//    private static final float PLAYER_SPEED = 120.0f;
//
//    private final Board board;
//    private GamePanel gamePanel;
//
//    // Cell size from GamePanel for positioning
//    private static final int CELL_SIZE = GamePanel.CELL_SIZE;
//
//    // Movement timing
//    private long lastMoveTime = 0;
//    private long lastInputTime = 0;
//    private static final long INPUT_DEBOUNCE_TIME = 100; // Milliseconds to wait between input changes
//
//    // Tile alignment tolerance (how close to the center we need to be to consider "aligned")
//    private static final float ALIGNMENT_TOLERANCE = 4.0f;
//
//    private boolean isMoving = false;
//
//    public Player(Board board, int startRow, int startCol) {
//        this.board = board;
//        this.row = startRow;
//        this.col = startCol;
//
//        // Initialize pixel position based on grid coordinates
//        this.pixelX = col * CELL_SIZE;
//        this.pixelY = row * CELL_SIZE;
//
//        // Initialize the last move time
//        this.lastMoveTime = System.currentTimeMillis();
//
//        loadAnimationFrames();
//        prepareAnimationIcons();
//    }
//
//    private void loadAnimationFrames() {
//        try {
//            directionFrames = new BufferedImage[4][2];
//
//            directionFrames[RIGHT][0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/right_half.png")));
//            directionFrames[RIGHT][1] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/right_full.png")));
//
//            directionFrames[DOWN][0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/down_half.png")));
//            directionFrames[DOWN][1] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/down_full.png")));
//
//            directionFrames[LEFT][0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/left_half.png")));
//            directionFrames[LEFT][1] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/left_full.png")));
//
//            directionFrames[UP][0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/up_half.png")));
//            directionFrames[UP][1] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/up_full.png")));
//
//            closedMouthFrame = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/pacman/closed.png")));
//
//        } catch (IOException | NullPointerException e) {
//            e.printStackTrace();
//        }
//
//        prepareAnimationIcons();
//    }
//
//    private void prepareAnimationIcons() {
//        animationIcons = new ImageIcon[3];
//        updateDirectionIcons();
//    }
//
//    private void updateDirectionIcons() {
//        if (directionFrames != null && closedMouthFrame != null) {
//            int size = GamePanel.CELL_SIZE;
//
//            Image halfOpen = directionFrames[direction][0].getScaledInstance(size, size, Image.SCALE_SMOOTH);
//            Image fullOpen = directionFrames[direction][1].getScaledInstance(size, size, Image.SCALE_SMOOTH);
//            Image closed = closedMouthFrame.getScaledInstance(size, size, Image.SCALE_SMOOTH);
//
//            animationIcons[0] = new ImageIcon(halfOpen);
//            animationIcons[1] = new ImageIcon(fullOpen);
//            animationIcons[2] = new ImageIcon(closed);
//        }
//    }
//
//
//    public void update(long currentTime) {
//        // Calculate delta time in seconds (for smooth movement)
//        float deltaTime = (currentTime - lastMoveTime) / 1000.0f;
//        lastMoveTime = currentTime;
//
//        // Cap deltaTime to avoid large jumps
//        if (deltaTime > 0.1f) {
//            deltaTime = 0.1f;
//        }
//
//        // Calculate the center pixel position of the current tile
//        float centerX = col * CELL_SIZE;
//        float centerY = row * CELL_SIZE;
//
//        // Check if we're near the center of a tile for direction changes
//        boolean isAlignedX = Math.abs(pixelX - centerX) < ALIGNMENT_TOLERANCE;
//        boolean isAlignedY = Math.abs(pixelY - centerY) < ALIGNMENT_TOLERANCE;
//
//        // Try to change direction at tile centers or when stopped
//        if (nextDirection != direction) {
//            // For opposite directions, allow immediate turning
//            boolean isOppositeDirection =
//                    (direction == RIGHT && nextDirection == LEFT) ||
//                            (direction == LEFT && nextDirection == RIGHT) ||
//                            (direction == UP && nextDirection == DOWN) ||
//                            (direction == DOWN && nextDirection == UP);
//
//            if (isOppositeDirection) {
//                direction = nextDirection;
//                updateDirectionIcons();
//            }
//            // For perpendicular turns, need to be aligned with the grid
//            else if ((isAlignedX && (nextDirection == UP || nextDirection == DOWN)) ||
//                    (isAlignedY && (nextDirection == LEFT || nextDirection == RIGHT))) {
//
//                // If aligned for the turn and can move in that direction
//                if (canMove(nextDirection)) {
//                    // Snap to the grid for precise turning
//                    if (nextDirection == UP || nextDirection == DOWN) {
//                        pixelX = centerX;
//                    } else {
//                        pixelY = centerY;
//                    }
//
//                    direction = nextDirection;
//                    updateDirectionIcons();
//                }
//            }
//        }
//
//        // Determine if we can move in the current direction
//        boolean canMoveInCurrentDirection = canMove(direction);
//
//        // Calculate movement distance based on speed and delta time
//        float moveDistance = PLAYER_SPEED * deltaTime;
//
//        // Only move if we can move in the current direction
//        if (canMoveInCurrentDirection) {
//            switch (direction) {
//                case RIGHT:
//                    pixelX += moveDistance;
//                    break;
//                case DOWN:
//                    pixelY += moveDistance;
//                    break;
//                case LEFT:
//                    pixelX -= moveDistance;
//                    break;
//                case UP:
//                    pixelY -= moveDistance;
//                    break;
//            }
//
//            // Update grid position if we've moved to a new tile
//            int newRow = Math.round(pixelY / CELL_SIZE);
//            int newCol = Math.round(pixelX / CELL_SIZE);
//
//            if (newRow != row || newCol != col) {
//                // We've entered a new tile
//                row = newRow;
//                col = newCol;
//
//                // Check for dots
//                char currentTile = board.getTile(row, col);
//                if (currentTile == '.') {
//                    board.updateTile(row, col, ' ');
//
//                    if (gamePanel != null) {
//                        gamePanel.clearDot(row, col);
//                    }
//                }
//            }
//        } else {
//            // If we can't move, align to the grid to avoid getting stuck
//            switch (direction) {
//                case RIGHT:
//                    pixelX = (col * CELL_SIZE) - 1;
//                    break;
//                case DOWN:
//                    pixelY = (row * CELL_SIZE) - 1;
//                    break;
//                case LEFT:
//                    pixelX = (col * CELL_SIZE) + 1;
//                    break;
//                case UP:
//                    pixelY = (row * CELL_SIZE) + 1;
//                    break;
//            }
//        }
//    }
//
//    // Check if player is aligned with current tile coordinates
//    private boolean isAlignedWithCurrentTile() {
//        return pixelX == col * CELL_SIZE && pixelY == row * CELL_SIZE;
//    }
//
//    public int getCurrentFrame() {
//        return currentFrame;
//    }
//
//    public void setAnimationFrame(int frame) {
//        if (frame >= 0 && frame < 3) {
//            this.currentFrame = frame;
//        }
//    }
//
//    public ImageIcon getCurrentIcon() {
//        if (animationIcons != null && currentFrame >= 0 && currentFrame < animationIcons.length) {
//            return animationIcons[currentFrame];
//        }
//        return null;
//    }
//
//    public void setNextDirection(int direction) {
//        if (direction >= 0 && direction <= 3) {
//            long currentTime = System.currentTimeMillis();
//
//            // Debounce input to prevent rapid direction changes
//            if (direction != nextDirection && currentTime - lastInputTime > INPUT_DEBOUNCE_TIME) {
//                this.nextDirection = direction;
//                lastInputTime = currentTime;
//            }
//        }
//    }
//
//    private boolean canMove(int direction) {
//        // Use current tile position for checking movement
//        int newRow = row;
//        int newCol = col;
//
//        switch (direction) {
//            case RIGHT:
//                newCol++;
//                break;
//            case DOWN:
//                newRow++;
//                break;
//            case LEFT:
//                newCol--;
//                break;
//            case UP:
//                newRow--;
//                break;
//        }
//
//        if (newRow < 0 || newRow >= board.getHeight() || newCol < 0 || newCol >= board.getWidth()) {
//            return false;
//        }
//
//        char tile = board.getTile(newRow, newCol);
//        return tile != '|' && tile != 'X';
//    }
//
//    private void move(int direction, float deltaTime) {
//        // Calculate movement distance based on speed and delta time
//        float distance = PLAYER_SPEED * deltaTime;
//
//        // Store previous position
//        float oldPixelX = pixelX;
//        float oldPixelY = pixelY;
//
//        // Update pixel position based on direction
//        switch (direction) {
//            case RIGHT:
//                pixelX += distance;
//                break;
//            case DOWN:
//                pixelY += distance;
//                break;
//            case LEFT:
//                pixelX -= distance;
//                break;
//            case UP:
//                pixelY -= distance;
//                break;
//        }
//
//        // Update grid position based on pixel position
//        int newRow = Math.round(pixelY / CELL_SIZE);
//        int newCol = Math.round(pixelX / CELL_SIZE);
//
//        // Check if we've moved to a new tile
//        if (newRow != row || newCol != col) {
//            // Make sure the new tile is valid (not a wall)
//            if (canMove(direction)) {
//                // If we've crossed into a new valid tile
//                int prevRow = row;
//                int prevCol = col;
//
//                row = newRow;
//                col = newCol;
//
//                // Check if we've collected a dot
//                char currentTile = board.getTile(row, col);
//                if (currentTile == '.') {
//                    board.updateTile(row, col, ' ');
//
//                    if (gamePanel != null) {
//                        gamePanel.clearDot(row, col);
//                    }
//                }
//            } else {
//                // If we've hit a wall, move back to the edge of the current tile
//                pixelX = oldPixelX;
//                pixelY = oldPixelY;
//
//                // Align to the grid
//                switch (direction) {
//                    case RIGHT:
//                        pixelX = col * CELL_SIZE + CELL_SIZE - 1;
//                        break;
//                    case DOWN:
//                        pixelY = row * CELL_SIZE + CELL_SIZE - 1;
//                        break;
//                    case LEFT:
//                        pixelX = col * CELL_SIZE;
//                        break;
//                    case UP:
//                        pixelY = row * CELL_SIZE;
//                        break;
//                }
//            }
//        }
//    }
//
//    public int getRow() {
//        return row;
//    }
//
//    public int getCol() {
//        return col;
//    }
//
//    public float getPixelX() {
//        return pixelX;
//    }
//
//    public float getPixelY() {
//        return pixelY;
//    }
//
//    public int getDirection() {
//        return direction;
//    }
//
//    public void reset(int startRow, int startCol) {
//        this.row = startRow;
//        this.col = startCol;
//        this.pixelX = col * CELL_SIZE;
//        this.pixelY = row * CELL_SIZE;
//        this.direction = RIGHT;
//        this.nextDirection = RIGHT;
//        this.isMoving = false;
//
//        long currentTime = System.currentTimeMillis();
//        this.lastMoveTime = currentTime;
//        this.lastInputTime = currentTime;
//
//        updateDirectionIcons();
//    }
//
//    public void setGamePanel(GamePanel gamePanel) {
//        this.gamePanel = gamePanel;
//    }
//}

