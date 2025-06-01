package game.upgrades;

import game.Player;

/**
 * Health restore power-up that gives the player an extra life
 */
public class HealthRestore extends Upgrade {
    
    /**
     * Creates a new health restore power-up
     * 
     * @param row The row position
     * @param col The column position
     */
    public HealthRestore(int row, int col) {
        super(row, col, 0, "/assets/upgrades/health.png"); // Instant effect (0 duration)
    }
    
    @Override
    public boolean applyEffect(Player player) {
        // Add an extra life
        player.addLife();
        return true;
    }
    
    @Override
    public void removeEffect(Player player) {
        // No need to remove effect for instant power-ups
    }
    
    @Override
    public String getName() {
        return "Extra Life";
    }
}
