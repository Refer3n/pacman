package game;

import javax.swing.*;

public class PacmanAnimator implements Runnable {
    private static final int ANIMATION_FRAMES = 3;
    private static final long FRAME_DELAY = 80;
    
    private final Player player;
    private final GamePanel gamePanel;
    private boolean running = false;
    private boolean paused = false;
    private Thread animationThread;
    private int currentFrame = 0;
    
    public PacmanAnimator(Player player, GamePanel gamePanel) {
        this.player = player;
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
            player.setAnimationFrame(currentFrame);

            if (gamePanel != null) {
                SwingUtilities.invokeLater(gamePanel::updatePlayerSprite);
            }

            try {
                Thread.sleep(FRAME_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public int getCurrentFrame() {
        return currentFrame;
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
