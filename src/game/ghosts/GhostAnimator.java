package game.ghosts;

import game.GamePanel;

import javax.swing.*;
import java.util.List;

public class GhostAnimator implements Runnable {
    private static final int ANIMATION_FRAMES = 2;
    private static final long FRAME_DELAY = 250;
    
    private final List<Ghost> ghosts;
    private final GamePanel gamePanel;
    private boolean running = false;
    private boolean paused = false;
    private Thread animationThread;
    private int currentFrame = 0;

    public GhostAnimator(List<Ghost> ghosts, GamePanel gamePanel) {
        this.ghosts = ghosts;
        this.gamePanel = gamePanel;
    }

    public void start() {
        if (running) return;
        
        running = true;
        animationThread = new Thread(this);
        animationThread.setDaemon(true);
        animationThread.start();
    }

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

            currentFrame = (currentFrame + 1) % ANIMATION_FRAMES;
            
            for (Ghost ghost : ghosts) {
                ghost.setAnimationFrame(currentFrame);
            }

            SwingUtilities.invokeLater(gamePanel::updateGhostSprites);
            
            try {
                Thread.sleep(FRAME_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public synchronized void pause() {
        this.paused = true;
    }

    public synchronized void resume() {
        this.paused = false;
        notifyAll();
    }
}
