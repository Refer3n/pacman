package game;

import board.Board;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import game.ghosts.*;
import game.pacman.Pacman;
import game.pacman.PacmanAnimator;
import game.upgrades.*;

public class GameLoop {
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;

    private final GamePanel gamePanel;
    private GameWindow gameWindow;
    private final Pacman pacman;
    private final PacmanAnimator pacmanAnimator;
    private final GhostAnimator ghostAnimator;
    private final List<Ghost> ghosts = new ArrayList<>();

    private UpgradeManager upgradeManager;
    private UpgradeSpawner upgradeSpawner;

    private final Thread gameThread;
    private boolean running = false;
    private boolean paused = false;

    public GameLoop(Board board, GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        char[][] layout = board.getLayout();
        int startRow = -1, startCol = -1;
        int ghostHomeRow = board.getHeight() / 2;
        int ghostHomeCol = board.getWidth() / 2;

        outer:
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                if (layout[row][col] == 'P') {
                    startRow = row;
                    startCol = col;
                    board.updateTile(row, col, ' ');
                    break outer;
                }
            }
        }

        outer:
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                if (layout[row][col] == 'G') {
                    ghostHomeRow = row;
                    ghostHomeCol = col;
                    board.updateTile(row, col, ' ');
                    break outer;
                }
            }
        }

        pacman = new Pacman(board, startRow, startCol);
        pacman.setGamePanel(gamePanel);
        gamePanel.setPlayer(pacman);

        initializeGhosts(board, ghostHomeRow, ghostHomeCol);
        pacmanAnimator = new PacmanAnimator(pacman, gamePanel);
        ghostAnimator = new GhostAnimator(ghosts, gamePanel);

        initializePowerUps(board);

        gameThread = new Thread(this::runGameLoop);
        gameThread.setDaemon(true);
    }

    private void initializePowerUps(Board board) {
        upgradeManager = new UpgradeManager(board);
        upgradeManager.setBoardPanel(gamePanel.getBoardPanel());

        if (!gamePanel.getGhosts().isEmpty()) {
            upgradeSpawner = new UpgradeSpawner(gamePanel.getGhosts(), upgradeManager, board);
        }
    }

    private void initializeGhosts(Board board, int homeRow, int homeCol) {
        ghosts.add(new Blinky(board, homeRow, homeCol));
        ghosts.add(new Pinky(board, homeRow, homeCol - 1));
        ghosts.add(new Inky(board, homeRow, homeCol + 1));

        for (Ghost ghost : ghosts) {
            gamePanel.addGhost(ghost);
        }
    }

    private void runGameLoop() {
        long lastUpdateTime = System.currentTimeMillis();

        while (running) {
            synchronized (this) {
                while (paused) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= FRAME_DELAY) {
                update();
                lastUpdateTime = currentTime;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void start() {
        if (running) return;
        running = true;
        pacmanAnimator.start();
        ghostAnimator.start();
        upgradeSpawner.start();
        gameThread.start();
    }

    public void stop() {
        if (!running) return;
        running = false;
        pacmanAnimator.stop();
        ghostAnimator.stop();
        upgradeSpawner.stop();
    }

    private void update() {
        long currentTime = System.currentTimeMillis();

        pacman.update(currentTime);
        checkAndApplyPowerUps(currentTime);

        for (Ghost ghost : ghosts) {
            ghost.update(currentTime, pacman.getRow(), pacman.getCol());
        }

        checkPlayerGhostCollisions();

        gamePanel.updatePlayerPosition();
        gamePanel.updateGhostPositions();

        SwingUtilities.invokeLater(() -> {
            gamePanel.invalidate();
            gamePanel.repaint();
        });
    }

    private void checkAndApplyPowerUps(long currentTime) {
        Upgrade collectedUpgrade = upgradeManager.checkPowerUpCollection(pacman);

        if (collectedUpgrade != null && collectedUpgrade.applyEffect(pacman)) {
            if (collectedUpgrade.getDuration() > 0) {
                upgradeManager.activatePowerUp(collectedUpgrade, pacman);
            }

            int points = 50 * pacman.getScoreMultiplier();
            gameWindow.updateScore(points);

            if (collectedUpgrade instanceof HealthRestore) {
                gameWindow.updateLivesDisplay(pacman.getLives());
            }


            if (collectedUpgrade instanceof GhostKiller) {
                for (Ghost ghost : ghosts) {
                    if (ghost.getCurrentState() != GhostState.IN_HOME &&
                            ghost.getCurrentState() != GhostState.LEAVING_HOME) {
                        ghost.frighten();
                    }
                }
            }
        }

        upgradeManager.updateActiveEffects(pacman, currentTime);
    }

    private void checkPlayerGhostCollisions() {
        int playerRow = pacman.getRow();
        int playerCol = pacman.getCol();

        for (Ghost ghost : ghosts) {
            if (ghost.getRow() == playerRow && ghost.getCol() == playerCol) {
                if (ghost.getCurrentState() == GhostState.FRIGHTENED || pacman.isGhostKillerMode()) {
                    ghost.reset();
                    int points = 200 * pacman.getScoreMultiplier();
                    gameWindow.updateScore(points);

                }
                else if (ghost.getCurrentState() != GhostState.IN_HOME &&
                        ghost.getCurrentState() != GhostState.LEAVING_HOME) {
                    boolean hasLivesLeft = pacman.loseLife();

                    gameWindow.updateLivesDisplay(pacman.getLives());

                    if (!hasLivesLeft) {
                        gameWindow.showGameOver();
                        stop();
                    }

                    upgradeManager.removeAllUpgrades();
                    pacman.reset(playerRow, playerCol);
                    gamePanel.resetGhosts();
                    break;
                }
            }
        }
    }

    public void handleInput(int direction) {
        pacman.setNextDirection(direction);
    }

    public Pacman getPlayer() {
        return pacman;
    }

    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public synchronized void pause() {
        paused = true;
        pacmanAnimator.pause();
        ghostAnimator.pause();
        upgradeSpawner.pause();
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
        pacmanAnimator.resume();
        ghostAnimator.resume();
        upgradeSpawner.resume();
    }
}
