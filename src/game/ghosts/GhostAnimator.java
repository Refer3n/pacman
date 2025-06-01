package game.ghosts;

import game.GamePanel;

import javax.swing.*;
import java.util.List;

/**
 * Handles the animation for all ghosts
 */
public class GhostAnimator implements Runnable {
    private static final int ANIMATION_FRAMES = 2;
    private static final long FRAME_DELAY = 250; // Slower animation than Pacman
    
    private final List<Ghost> ghosts;
    private final GamePanel gamePanel;
    private boolean running = false;
    private boolean paused = false;
    private Thread animationThread;
    private int currentFrame = 0;
    
    /**
     * Creates a new ghost animator
     * 
     * @param ghosts List of ghosts to animate
     * @param gamePanel Game panel to update
     */
    public GhostAnimator(List<Ghost> ghosts, GamePanel gamePanel) {
        this.ghosts = ghosts;
        this.gamePanel = gamePanel;
    }
    
    /**
     * Starts the animation thread
     */
    public void start() {
        if (running) return;
        
        running = true;
        animationThread = new Thread(this);
        animationThread.setDaemon(true);
        animationThread.start();
    }
    
    /**
     * Stops the animation thread
     */
    public void stop() {
        running = false;
        if (animationThread != null) {
            try {
                animationThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Override
    public void run() {
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
            
            // Update animation frame for all ghosts
            currentFrame = (currentFrame + 1) % ANIMATION_FRAMES;
            
            for (Ghost ghost : ghosts) {
                ghost.setAnimationFrame(currentFrame);
            }
            
            // Update the UI
            if (gamePanel != null) {
                SwingUtilities.invokeLater(gamePanel::updateGhostSprites);
            }
            
            try {
                Thread.sleep(FRAME_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Pauses the animation thread
     */
    public synchronized void pause() {
        this.paused = true;
    }
    
    /**
     * Resumes the animation thread
     */
    public synchronized void resume() {
        this.paused = false;
        notifyAll();
    }
}
