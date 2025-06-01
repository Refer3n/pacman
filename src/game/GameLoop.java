package game;

import board.Board;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import game.ghosts.*;

/**
 * Game loop that coordinates game state updates and rendering
 */
public class GameLoop {
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;

    private final GamePanel gamePanel;
    private Player player;
        private PacmanAnimator pacmanAnimator;
        private GhostAnimator ghostAnimator;
        private List<Ghost> ghosts = new ArrayList<>();

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
    
            gameThread = new Thread(this::runGameLoop);
            gameThread.setDaemon(true);
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
    
            gameThread.start();
        }
    
    
        public void stop() {
            if (!running) return;
            running = false;
    
            pacmanAnimator.stop();
            ghostAnimator.stop();
    }

    private void update() {
            long currentTime = System.currentTimeMillis();
            
            // Update player
            player.update(currentTime);
            
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
         * Checks for collisions between player and ghosts
         */
        private void checkPlayerGhostCollisions() {
            int playerRow = player.getRow();
            int playerCol = player.getCol();
            
            for (Ghost ghost : ghosts) {
                if (Math.abs(ghost.getRow() - playerRow) <= 0 && 
                    Math.abs(ghost.getCol() - playerCol) <= 0) {

                    if (ghost.getCurrentState() == GhostState.FRIGHTENED) {
                        // Player eats the ghost
                        ghost.reset();
                    } else if (ghost.getCurrentState() != GhostState.IN_HOME && 
                               ghost.getCurrentState() != GhostState.LEAVING_HOME) {
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

    public synchronized void pause() {
        this.paused = true;
            if (pacmanAnimator != null) {
                pacmanAnimator.pause();
            }
            if (ghostAnimator != null) {
                ghostAnimator.pause();
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
    }
}
