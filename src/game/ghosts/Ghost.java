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

    protected int row, col;
    protected float pixelX, pixelY;
    protected int direction = RIGHT;
    protected int nextDirection = RIGHT;

    protected int currentFrame = 0;
    protected BufferedImage[][] directionFrames;
    protected ImageIcon[] animationIcons;

    protected GhostState currentState = GhostState.IN_HOME;
    protected long lastMoveTime = 0;
    protected long stateChangeTime;
    protected static final long SCATTER_DURATION = 7000;
    protected long lastTargetUpdateTime = 0;

    protected final String ghostName;
    protected final Color ghostColor;

    protected final Board board;
    protected GamePanel gamePanel;
    protected final Random random = new Random();

    protected final int scatterCornerRow, scatterCornerCol;
    protected int homeRow, homeCol;
    protected int exitRow, exitCol;
    protected int targetRow, targetCol;

    private static final float ALIGNMENT_TOLERANCE = 4.0f;
    protected float speed = BASE_GHOST_SPEED;

    public Ghost(Board board, int startRow, int startCol, String ghostName, Color color,
                 int scatterRow, int scatterCol) {
        this.board = board;
        this.ghostName = ghostName;
        this.ghostColor = color;

        this.row = startRow;
        this.col = startCol;
        this.homeRow = startRow;
        this.homeCol = startCol;

        this.scatterCornerRow = scatterRow;
        this.scatterCornerCol = scatterCol;

        this.targetRow = scatterRow;
        this.targetCol = scatterCol;

        this.exitRow = startRow - 2;
        this.exitCol = startCol;

        this.pixelX = getCellCenterX(col);
        this.pixelY = getCellCenterY(row);

        this.lastMoveTime = System.currentTimeMillis();
        this.stateChangeTime = lastMoveTime + SCATTER_DURATION;

        loadAnimationFrames();
    }

    private void loadAnimationFrames() {
        directionFrames = new BufferedImage[4][2];
        String[] directions = {"right", "down", "left", "up"};

        try {
            for (int dir = 0; dir < directions.length; dir++) {
                directionFrames[dir][0] = loadFrame(directions[dir], "1");
                directionFrames[dir][1] = loadFrame(directions[dir], "2");
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        prepareAnimationIcons();
    }

    private BufferedImage loadFrame(String direction, String frameNumber) throws IOException {
        String path = String.format("/assets/ghosts/%s/%s_%s.png", ghostName, direction, frameNumber);
        return ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(path)));
    }

    private void prepareAnimationIcons() {
        animationIcons = new ImageIcon[2];
        updateDirectionIcons();
    }

    protected void updateDirectionIcons() {
        if (directionFrames == null) return;

        int size = GamePanel.CELL_SIZE;
        BufferedImage frame1 = directionFrames[direction][0];
        BufferedImage frame2 = directionFrames[direction][1];

        if (animationIcons[0] == null || animationIcons[1] == null ||
                animationIcons[0].getIconWidth() != size || animationIcons[0].getIconHeight() != size) {
            animationIcons[0] = new ImageIcon(frame1.getScaledInstance(size, size, Image.SCALE_SMOOTH));
            animationIcons[1] = new ImageIcon(frame2.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } else {
            animationIcons[0].setImage(frame1.getScaledInstance(size, size, Image.SCALE_SMOOTH));
            animationIcons[1].setImage(frame2.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        }
    }

    public void update(long currentTime, int playerRow, int playerCol) {
        float deltaTime = (currentTime - lastMoveTime) / 1000.0f;
        lastMoveTime = currentTime;

        if (deltaTime > 0.1f) deltaTime = 0.1f;

        checkStateTransition(currentTime);

        switch (currentState) {
            case IN_HOME -> moveInHome(deltaTime);
            case LEAVING_HOME -> leavingHome(deltaTime);
            case CHASE -> chase(deltaTime, playerRow, playerCol);
            case SCATTER -> moveToScatterPosition(deltaTime);
            case FRIGHTENED -> moveFrightened(deltaTime);
        }

        ensureGridAlignment();
    }

    private float getCellCenterX(int col) {
        return col * GamePanel.CELL_SIZE;
    }

    private float getCellCenterY(int row) {
        return row * GamePanel.CELL_SIZE;
    }

    private void ensureGridAlignment() {
        float centerX = getCellCenterX(col);
        float centerY = getCellCenterY(row);

        if ((direction == LEFT || direction == RIGHT) && Math.abs(pixelY - centerY) < ALIGNMENT_TOLERANCE / 2) {
            pixelY = centerY;
        }

        if ((direction == UP || direction == DOWN) && Math.abs(pixelX - centerX) < ALIGNMENT_TOLERANCE / 2) {
            pixelX = centerX;
        }
    }

    private void checkStateTransition(long currentTime) {
        if (currentState == GhostState.SCATTER && currentTime > stateChangeTime) {
            currentState = GhostState.CHASE;
            reverseDirection();
        }

        if (currentState == GhostState.IN_HOME && shouldLeaveHome(currentTime)) {
            currentState = GhostState.LEAVING_HOME;
        }
    }

    private void reverseDirection() {
        direction = switch (direction) {
            case RIGHT -> LEFT;
            case LEFT -> RIGHT;
            case UP -> DOWN;
            case DOWN -> UP;
            default -> direction;
        };
        nextDirection = direction;

        targetRow = scatterCornerRow;
        targetCol = scatterCornerCol;
        lastTargetUpdateTime = System.currentTimeMillis();
    }

    protected boolean shouldLeaveHome(long currentTime) {
        return (currentTime - stateChangeTime) > 3000;
    }

    private void moveInHome(float deltaTime) {
        float moveDistance = speed * 0.5f * deltaTime;
        float centerY = getCellCenterY(homeRow);

        if (direction == UP) {
            pixelY -= moveDistance;
            if (pixelY < centerY - GamePanel.CELL_SIZE * 0.5f) direction = DOWN;
        } else {
            pixelY += moveDistance;
            if (pixelY > centerY + GamePanel.CELL_SIZE * 0.5f) direction = UP;
        }
    }

    private void leavingHome(float deltaTime) {
        float moveDistance = speed * 0.7f * deltaTime;
        float targetX = getCellCenterX(exitCol);
        float targetY = getCellCenterY(exitRow);

        if (Math.abs(pixelY - targetY) > ALIGNMENT_TOLERANCE) {
            direction = (pixelY > targetY) ? UP : DOWN;
            pixelY += (direction == UP ? -moveDistance : moveDistance);

            if (Math.abs(pixelX - targetX) < ALIGNMENT_TOLERANCE * 2) {
                pixelX = targetX;
            }
        } else if (Math.abs(pixelX - targetX) > ALIGNMENT_TOLERANCE) {
            pixelY = targetY;
            direction = (pixelX > targetX) ? LEFT : RIGHT;
            pixelX += (direction == LEFT ? -moveDistance : moveDistance);
        } else {
            pixelX = targetX;
            pixelY = targetY;
            row = exitRow;
            col = exitCol;

            direction = UP;
            nextDirection = UP;
            updateDirectionIcons();
            currentState = GhostState.CHASE;
        }
    }

    protected abstract void chase(float deltaTime, int playerRow, int playerCol);

    private void moveToScatterPosition(float deltaTime) {
        decideNextDirection(scatterCornerRow, scatterCornerCol);
        moveInDirection(deltaTime);
    }

    private void moveFrightened(float deltaTime) {
        if (isAtIntersection()) {
            int[] options = getAvailableDirections();
            if (options.length > 0) nextDirection = options[random.nextInt(options.length)];
        }
        moveInDirection(deltaTime);
    }

    protected void decideNextDirection(int targetRow, int targetCol) {
        if (isAtIntersection() || !canMove(direction)) {
            int[] available = getAvailableDirections();

            if (available.length == 1) {
                nextDirection = available[0];
                return;
            }

            int bestDirection = direction;
            double closestDistance = Double.MAX_VALUE;

            for (int dir : available) {
                if (isOppositeDirection(dir, direction)) continue;

                int tempRow = row, tempCol = col;
                switch (dir) {
                    case RIGHT -> tempCol++;
                    case LEFT -> tempCol--;
                    case UP -> tempRow--;
                    case DOWN -> tempRow++;
                }

                double dist = distanceSquared(tempRow, tempCol, targetRow, targetCol);
                if (dist < closestDistance) {
                    closestDistance = dist;
                    bestDirection = dir;
                }
            }

            nextDirection = bestDirection;
        } else {
            nextDirection = direction;
        }
    }

    protected void moveInDirection(float deltaTime) {
        float moveDistance = speed * deltaTime;
        float centerX = getCellCenterX(col);
        float centerY = getCellCenterY(row);

        boolean alignedX = Math.abs(pixelX - centerX) < ALIGNMENT_TOLERANCE;
        boolean alignedY = Math.abs(pixelY - centerY) < ALIGNMENT_TOLERANCE;

        if (alignedX && alignedY && nextDirection != direction) {
            if (canMove(nextDirection) || isOppositeDirection(direction, nextDirection)) {
                pixelX = centerX;
                pixelY = centerY;
                direction = nextDirection;
                updateDirectionIcons();
            }
        }

        if (!canMove(direction)) {
            int[] options = getAvailableDirections();
            if (options.length > 0) {
                nextDirection = options[random.nextInt(options.length)];
                direction = nextDirection;
                updateDirectionIcons();

                if (direction == UP || direction == DOWN) pixelX = centerX;
                else pixelY = centerY;
            }
            return;
        }

        float prevX = pixelX, prevY = pixelY;
        switch (direction) {
            case RIGHT -> pixelX += moveDistance;
            case LEFT -> pixelX -= moveDistance;
            case UP -> pixelY -= moveDistance;
            case DOWN -> pixelY += moveDistance;
        }

        int newRow = Math.round(pixelY / GamePanel.CELL_SIZE);
        int newCol = Math.round(pixelX / GamePanel.CELL_SIZE);

        if (newRow != row || newCol != col) {
            if (canMove(direction)) {
                row = newRow;
                col = newCol;
            } else {
                pixelX = prevX;
                pixelY = prevY;
            }
        }
    }

    private double distanceSquared(int x1, int y1, int x2, int y2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
    }

    protected boolean canMove(int direction) {
        if (currentState == GhostState.IN_HOME) {
            return switch (direction) {
                case UP -> pixelY > (homeRow - 0.5f) * GamePanel.CELL_SIZE;
                case DOWN -> pixelY < (homeRow + 0.5f) * GamePanel.CELL_SIZE;
                default -> false;
            };
        }

        if (currentState == GhostState.LEAVING_HOME) {
            return (direction == UP && row >= exitRow)
                    || (direction == LEFT && col > exitCol)
                    || (direction == RIGHT && col < exitCol);
        }

        int newRow = row, newCol = col;
        switch (direction) {
            case RIGHT -> newCol++;
            case LEFT -> newCol--;
            case UP -> newRow--;
            case DOWN -> newRow++;
        }

        if (newRow < 0 || newRow >= board.getHeight() || newCol < 0 || newCol >= board.getWidth())
            return false;

        char tile = board.getTile(newRow, newCol);
        return tile != '|' && tile != 'X';
    }

    protected boolean isAtIntersection() {
        return Math.abs(pixelX - getCellCenterX(col)) < ALIGNMENT_TOLERANCE &&
                Math.abs(pixelY - getCellCenterY(row)) < ALIGNMENT_TOLERANCE &&
                getAvailableDirections().length > 2;
    }

    public int[] getAvailableDirections() {
        int[] directions = new int[4];
        int count = 0;

        if (canMove(RIGHT)) directions[count++] = RIGHT;
        if (canMove(DOWN)) directions[count++] = DOWN;
        if (canMove(LEFT)) directions[count++] = LEFT;
        if (canMove(UP)) directions[count++] = UP;

        int[] result = new int[count];
        System.arraycopy(directions, 0, result, 0, count);
        return result;
    }

    protected boolean isOppositeDirection(int d1, int d2) {
        return (d1 == RIGHT && d2 == LEFT) || (d1 == LEFT && d2 == RIGHT) ||
                (d1 == UP && d2 == DOWN) || (d1 == DOWN && d2 == UP);
    }

    public void frighten() {
        if (currentState != GhostState.IN_HOME && currentState != GhostState.LEAVING_HOME) {
            currentState = GhostState.FRIGHTENED;
            reverseDirection();
        }
    }

    public void reset() {
        row = homeRow;
        col = homeCol;
        pixelX = getCellCenterX(col);
        pixelY = getCellCenterY(row);
        direction = RIGHT;
        nextDirection = RIGHT;
        currentState = GhostState.IN_HOME;
        stateChangeTime = System.currentTimeMillis() + 3000;
        updateDirectionIcons();
    }

    public ImageIcon getCurrentIcon() {
        return animationIcons != null && currentFrame >= 0 && currentFrame < animationIcons.length
                ? animationIcons[currentFrame]
                : null;
    }

    public void setAnimationFrame(int frame) {
        if (frame >= 0 && frame < 2) currentFrame = frame;
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public float getPixelX() { return pixelX; }
    public float getPixelY() { return pixelY; }
    public GhostState getCurrentState() { return currentState; }
}
