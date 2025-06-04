package game.pacman;

import game.GamePanel;
import javax.swing.*;

public class PacmanAnimator implements Runnable {
    private static final int ANIMATION_FRAMES = 3;
    private static final long FRAME_DELAY = 80;

    private final Pacman player;
    private final GamePanel gamePanel;

    private Thread animationThread;
    private boolean running = false;
    private boolean paused = false;
    private int currentFrame = 0;

    public PacmanAnimator(Pacman pacman, GamePanel gamePanel) {
        this.player = pacman;
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
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            currentFrame = (currentFrame + 1) % ANIMATION_FRAMES;
            player.setAnimationFrame(currentFrame);

            SwingUtilities.invokeLater(gamePanel::updatePlayerSprite);

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
