package game.powerups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import board.Board;
import game.BoardPanel;
import game.Player;

/**
 * Manages all power-ups in the game
 */
public class PowerUpManager {
    
    // List of all power-ups on the board
    private List<PowerUp> powerUps = new ArrayList<>();
    
    // Map of active effects and their expiration times
    private Map<PowerUp, Long> activeEffects = new HashMap<>();
    
    private final Board board;
    private final Random random = new Random();
    private BoardPanel boardPanel;
    
    /**
     * Creates a new power-up manager
     * 
     * @param board The game board
     */
    public PowerUpManager(Board board) {
        this.board = board;
    }
    
    /**
     * Sets the board panel for rendering
     */
    public void setBoardPanel(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
    }
    
    /**
     * Spawns a power-up at a random empty location
     * 
     * @return The spawned power-up, or null if spawning failed
     */
    public PowerUp spawnRandomPowerUp() {
        // Find empty spaces on the board
        List<int[]> emptySpaces = new ArrayList<>();
        
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                if (board.getTile(row, col) == ' ') {
                    // Check if there's already a power-up here
                    boolean hasPowerUp = false;
                    for (PowerUp powerUp : powerUps) {
                        if (!powerUp.isCollected() && powerUp.getRow() == row && powerUp.getCol() == col) {
                            hasPowerUp = true;
                            break;
                        }
                    }
                    
                    if (!hasPowerUp) {
                        emptySpaces.add(new int[]{row, col});
                    }
                }
            }
        }
        
        // If no empty spaces, return null
        if (emptySpaces.isEmpty()) {
            return null;
        }
        
        // Choose a random empty space
        int[] position = emptySpaces.get(random.nextInt(emptySpaces.size()));
        int row = position[0];
        int col = position[1];
        
        // Create and return a power-up at this position
        return createPowerUp(row, col);
    }
    
    /**
     * Creates a power-up at the specified position
     * 
     * @param row The row position
     * @param col The column position
     * @return The created power-up, or null if creation failed
     */
    public PowerUp createPowerUp(int row, int col) {
        // Check if there's already a power-up at this position
        for (PowerUp powerUp : powerUps) {
            if (!powerUp.isCollected() && powerUp.getRow() == row && powerUp.getCol() == col) {
                return null; // Position already has a power-up
            }
        }
        
        // Create a random power-up
        PowerUp powerUp;
        int powerUpType = random.nextInt(4);
        
        switch (powerUpType) {
            case 0:
                powerUp = new SpeedBoost(row, col);
                break;
            case 1:
                powerUp = new DoubleScore(row, col);
                break;
            case 2:
                powerUp = new HealthRestore(row, col);
                break;
            case 3:
                powerUp = new GhostKiller(row, col);
                break;
            default:
                powerUp = new SpeedBoost(row, col);
        }
        
        // Add to list
        powerUps.add(powerUp);
        
        // Update the board panel with the new power-up
        if (boardPanel != null) {
            boardPanel.addPowerUp(row, col, powerUp);
        }
        
        return powerUp;
    }
    
    /**
     * Checks if the player has collected any power-ups
     * 
     * @param player The player
     * @return The collected power-up or null if none
     */
    public PowerUp checkPowerUpCollection(Player player) {
        int playerRow = player.getRow();
        int playerCol = player.getCol();
        PowerUp collectedPowerUp = null;
        
        for (PowerUp powerUp : powerUps) {
            if (!powerUp.isCollected() && powerUp.getRow() == playerRow && powerUp.getCol() == playerCol) {
                // Collect the power-up
                powerUp.setCollected(true);
                collectedPowerUp = powerUp;
                
                // Remove from board panel
                if (boardPanel != null) {
                    boardPanel.removePowerUp(powerUp.getRow(), powerUp.getCol());
                }
                
                break;
            }
        }
        
        return collectedPowerUp;
    }
    
    /**
     * Activates a power-up effect
     * 
     * @param powerUp The power-up to activate
     * @param player The player to apply the effect to
     */
    public void activatePowerUp(PowerUp powerUp, Player player) {
        // Apply the effect
        if (powerUp.applyEffect(player)) {
            // If it has a duration, add to active effects
            if (powerUp.getDuration() > 0) {
                long expirationTime = System.currentTimeMillis() + powerUp.getDuration();
                activeEffects.put(powerUp, expirationTime);
            }
            
            // Notify about the power-up
            System.out.println("Activated " + powerUp.getName() + "!");
        }
    }
    
    /**
     * Updates active power-up effects and removes expired ones
     * 
     * @param player The player
     * @param currentTime The current time
     * @return List of effects that just expired this update
     */
    public List<PowerUp> updateActiveEffects(Player player, long currentTime) {
        List<PowerUp> expiredEffects = new ArrayList<>();
        
        // Check for expired effects
        for (Map.Entry<PowerUp, Long> entry : new HashMap<>(activeEffects).entrySet()) {
            PowerUp powerUp = entry.getKey();
            long expirationTime = entry.getValue();
            
            if (currentTime >= expirationTime) {
                // Remove effect
                powerUp.removeEffect(player);
                activeEffects.remove(powerUp);
                expiredEffects.add(powerUp);
                
                System.out.println(powerUp.getName() + " effect expired");
            }
        }
        
        return expiredEffects;
    }
    
    /**
     * Gets all power-ups that haven't been collected
     * 
     * @return List of uncollected power-ups
     */
    public List<PowerUp> getAvailablePowerUps() {
        List<PowerUp> available = new ArrayList<>();
        
        for (PowerUp powerUp : powerUps) {
            if (!powerUp.isCollected()) {
                available.add(powerUp);
            }
        }
        
        return available;
    }
    
    /**
     * Gets all currently active power-up effects
     * 
     * @return Map of active power-ups and their remaining time
     */
    public Map<PowerUp, Long> getActiveEffects() {
        // Calculate remaining time for each effect
        Map<PowerUp, Long> remainingTime = new HashMap<>();
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<PowerUp, Long> entry : activeEffects.entrySet()) {
            PowerUp powerUp = entry.getKey();
            long expirationTime = entry.getValue();
            long remaining = Math.max(0, expirationTime - currentTime);
            
            remainingTime.put(powerUp, remaining);
        }
        
        return remainingTime;
    }
    
    /**
     * Clears all power-ups
     */
    public void clearPowerUps() {
        powerUps.clear();
        activeEffects.clear();
    }
}
