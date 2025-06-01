package game.powerups;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import board.Board;
import game.ghosts.Ghost;
import game.ghosts.GhostState;

/**
 * Manages spawning of power-ups by ghosts
 */
public class PowerUpSpawner implements Runnable {

    private static final long SPAWN_CHECK_INTERVAL = 5000;
    private static final float SPAWN_PROBABILITY = 0.25f;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private Thread spawnerThread;

    private final List<Ghost> ghosts;
    private final PowerUpManager powerUpManager;
    private final Board board;
    private final Random random = new Random();

    public PowerUpSpawner(List<Ghost> ghosts, PowerUpManager powerUpManager, Board board) {
        this.ghosts = ghosts;
        this.powerUpManager = powerUpManager;
        this.board = board;
    }

    public void start() {
        if (running.get()) {
            return;
        }
        
        running.set(true);
        spawnerThread = new Thread(this);
        spawnerThread.setDaemon(true);
        spawnerThread.start();
    }

    public void stop() {
        running.set(false);
        if (spawnerThread != null) {
            try {
                spawnerThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void pause() {
        paused.set(true);
    }

    public synchronized void resume() {
        paused.set(false);
        notifyAll();
    }
    
    @Override
    public void run() {
        while (running.get()) {
            synchronized (this) {
                while (paused.get()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            for (Ghost ghost : ghosts) {
                trySpawnPowerUp(ghost);
            }

            try {
                Thread.sleep(SPAWN_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    

    private void trySpawnPowerUp(Ghost ghost) {
        GhostState state = ghost.getCurrentState();
        if (state == GhostState.CHASE || state == GhostState.SCATTER) {
            if (random.nextFloat() < SPAWN_PROBABILITY) {
                int row = ghost.getRow();
                int col = ghost.getCol();

                if (isValidPowerUpPosition(row, col)) {
                    createPowerUpAtPosition(row, col);
                }
            }
        }
    }

    private boolean isValidPowerUpPosition(int row, int col) {
        if (row < 0 || row >= board.getHeight() || col < 0 || col >= board.getWidth()) {
            return false;
        }

        char tile = board.getTile(row, col);
        return tile == ' ';
    }

    private PowerUp createPowerUpAtPosition(int row, int col) {
        // Let the PowerUpManager create the power-up
        return powerUpManager.createPowerUp(row, col);
    }
}