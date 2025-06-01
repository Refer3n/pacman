package game;

import board.Board;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import game.ghosts.*;
import game.upgrades.*;

public class GameLoop {
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;

    private final GamePanel gamePanel;
    private GameWindow gameWindow;
    private Player player;
    private PacmanAnimator pacmanAnimator;
    private GhostAnimator ghostAnimator;
    private List<Ghost> ghosts = new ArrayList<>();

    private UpgradeManager upgradeManager;
    private UpgradeSpawner upgradeSpawner;

    private Thread gameThread;
    private boolean running = false;
    private boolean paused = false;

    public GameLoop(Board board, GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        int startRow = -1;
        int startCol = -1;
            int ghostHomeRow = -1;
            int ghostHomeCol = -1;
            char[][] layout = board.getLayout();

            for (int row = 0; row < board.getHeight(); row++) {
                for (int col = 0; col < board.getWidth(); col++) {
                    if (layout[row][col] == 'P') {
                        startRow = row;
                        startCol = col;
                        board.updateTile(row, col, ' ');
                        break;
                    }
                }
                if (startRow != -1) break;
            }

            ghostHomeRow = board.getHeight() / 2;
            ghostHomeCol = board.getWidth() / 2;

            // Find a position marked with 'G' for ghost house
            for (int row = 0; row < board.getHeight(); row++) {
                for (int col = 0; col < board.getWidth(); col++) {
                    if (layout[row][col] == 'G') {
                        ghostHomeRow = row;
                        ghostHomeCol = col;
                        board.updateTile(row, col, ' ');
                        break;
                    }
                }
            }

            // Initialize player
            player = new Player(board, startRow, startCol);
            player.setGamePanel(gamePanel);
            gamePanel.setPlayer(player);

            // Initialize ghosts
            initializeGhosts(board, ghostHomeRow, ghostHomeCol);

            // Initialize animators
            pacmanAnimator = new PacmanAnimator(player, gamePanel);
            ghostAnimator = new GhostAnimator(ghosts, gamePanel);

                // Initialize power-up system
                initializePowerUps(board);
    
                gameThread = new Thread(this::runGameLoop);
                gameThread.setDaemon(true);
            }
            
        /**
         * Initializes the power-up manager and spawner
         */
        private void initializePowerUps(Board board) {
            // Initialize power-up manager
            upgradeManager = new UpgradeManager(board);
            
            // Connect to the board panel
            if (gamePanel != null && gamePanel.getBoardPanel() != null) {
                upgradeManager.setBoardPanel(gamePanel.getBoardPanel());
            }
            
            // Initialize ghost-based power-up spawner
            if (gamePanel != null && !gamePanel.getGhosts().isEmpty()) {
                upgradeSpawner = new UpgradeSpawner(
                    gamePanel.getGhosts(), upgradeManager, board);
            }
        }

        /**
         * Initializes the ghosts for the game
         */
        private void initializeGhosts(Board board, int homeRow, int homeCol) {
            // Create the three ghost types
            Ghost blinky = new Blinky(board, homeRow, homeCol);
            Ghost pinky = new Pinky(board, homeRow, homeCol - 1);
            Ghost inky = new Inky(board, homeRow, homeCol + 1);

            ghosts.add(blinky);
            ghosts.add(pinky);
            ghosts.add(inky);

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
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - lastUpdateTime;

            if (elapsed >= FRAME_DELAY) {
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
    
            // Start power-up spawner
            if (upgradeSpawner != null) {
                upgradeSpawner.start();
            }
    
            gameThread.start();
        }
    
    
        public void stop() {
            if (!running) return;
            running = false;
    
            pacmanAnimator.stop();
            ghostAnimator.stop();
                
                if (upgradeSpawner != null) {
                    upgradeSpawner.stop();
                }
    }

    private void update() {
            long currentTime = System.currentTimeMillis();
            
            // Update player
            player.update(currentTime);
            
            // Check for power-up collection and apply effects
            checkAndApplyPowerUps(currentTime);
            
            // Update all ghosts
            for (Ghost ghost : ghosts) {
                ghost.update(currentTime, player.getRow(), player.getCol());
            }
            
            // Check for collisions between player and ghosts
            checkPlayerGhostCollisions();
    
            // Update positions on screen
            gamePanel.updatePlayerPosition();
            gamePanel.updateGhostPositions();
    
            SwingUtilities.invokeLater(() -> {
                gamePanel.invalidate();
                gamePanel.repaint();
            });
        }
        
    /**
     * Checks for power-up collection and applies their effects
     * 
     * @param currentTime The current game time
     */
    private void checkAndApplyPowerUps(long currentTime) {
        if (upgradeManager == null) return;
        
        // Check if the player has collected a power-up
        Upgrade collectedUpgrade = upgradeManager.checkPowerUpCollection(player);
        
        // If a power-up was collected, apply its effect
        if (collectedUpgrade != null) {
            // Apply the power-up effect
            boolean effectApplied = collectedUpgrade.applyEffect(player);
            
            if (effectApplied) {
                // Add to active effects for duration-based power-ups
                if (collectedUpgrade.getDuration() > 0) {
                    upgradeManager.activatePowerUp(collectedUpgrade, player);
                }
                
                // Show notification about the power-up
                String powerUpMessage = "Collected " + collectedUpgrade.getName() + "!";
                System.out.println(powerUpMessage);
                
                // Give points for collecting the power-up
                if (gameWindow != null) {
                    int basePoints = 50;
                    int points = basePoints * player.getScoreMultiplier();
                    gameWindow.updateScore(points);
                    
                    // Update lives display if it's a health restore upgrade
                    if (collectedUpgrade instanceof game.upgrades.HealthRestore) {
                        gameWindow.updateLivesDisplay(player.getLives());
                    }
                }
                
                // Apply special effects based on power-up type
                if (collectedUpgrade instanceof GhostKiller) {
                    // Make all ghosts frightened
                    for (Ghost ghost : ghosts) {
                        if (ghost.getCurrentState() != GhostState.IN_HOME && 
                            ghost.getCurrentState() != GhostState.LEAVING_HOME) {
                            ghost.frighten();
                        }
                    }
                }
            }
        }
        
        // Update active power-up effects and check for expired ones
        List<Upgrade> expiredUpgrades = upgradeManager.updateActiveEffects(player, currentTime);
        
        // Handle effects of expired power-ups
        for (Upgrade expiredUpgrade : expiredUpgrades) {
            // Special handling for ghost killer expiration
            if (expiredUpgrade instanceof GhostKiller) {
                System.out.println("Ghost killer mode expired");
            }
        }
    }
        
        /**
         * Checks for collisions between player and ghosts
         */
        private void checkPlayerGhostCollisions() {
            int playerRow = player.getRow();
            int playerCol = player.getCol();
            
            for (Ghost ghost : ghosts) {
                if (Math.abs(ghost.getRow() - playerRow) <= 0 && 
                    Math.abs(ghost.getCol() - playerCol) <= 0) {

                    if (ghost.getCurrentState() == GhostState.FRIGHTENED || player.isGhostKillerMode()) {
                        // Player eats the ghost
                        ghost.reset();
                        
                        // Award points for eating ghost
                        if (gameWindow != null) {
                            int basePoints = 200;
                            int points = basePoints * player.getScoreMultiplier();
                            gameWindow.updateScore(points);
                        }
                    } else if (ghost.getCurrentState() != GhostState.IN_HOME && 
                               ghost.getCurrentState() != GhostState.LEAVING_HOME) {
                        // Player loses a life
                        boolean hasLivesLeft = player.loseLife();
                        
                        // Update lives display in game window
                        if (gameWindow != null) {
                            gameWindow.updateLivesDisplay(player.getLives());
                        }
                        
                        if (!hasLivesLeft) {
                            // Game over
                            System.out.println("Game Over!");
                            if (gameWindow != null) {
                                gameWindow.showGameOver();
                            }
                            stop();
                        }
                        
                        if (upgradeManager != null) {
                            upgradeManager.removeAllUpgrades(false);
                        }
                        
                        // Reset positions
                        player.reset(player.getRow(), player.getCol());
                        gamePanel.resetGhosts();
                        break;
                    }
                }
            }
    }

    public void handleInput(int direction) {
        player.setNextDirection(direction);
    }

        public Player getPlayer() {
        return player;
    }

        /**
         * Sets the game window reference for score updates
         * 
         * @param gameWindow The game window
         */
        public void setGameWindow(GameWindow gameWindow) {
            this.gameWindow = gameWindow;
        }
        
    public synchronized void pause() {
        this.paused = true;
            if (pacmanAnimator != null) {
                pacmanAnimator.pause();
            }
            if (ghostAnimator != null) {
                ghostAnimator.pause();
            }
            if (upgradeSpawner != null) {
                upgradeSpawner.pause();
            }
        }
    
        public synchronized void resume() {
            this.paused = false;
            notifyAll();
            if (pacmanAnimator != null) {
                pacmanAnimator.resume();
            }
            if (ghostAnimator != null) {
                ghostAnimator.resume();
            }
            if (upgradeSpawner != null) {
                upgradeSpawner.resume();
            }
        }
    }

