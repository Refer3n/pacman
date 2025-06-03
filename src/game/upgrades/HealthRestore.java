package game.upgrades;

import game.pacman.Pacman;

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
    public boolean applyEffect(Pacman pacman) {
        // Add an extra life
        pacman.addLife();
        return true;
    }
    
    @Override
    public void removeEffect(Pacman pacman) {
        // No need to remove effect for instant power-ups
    }
    
    @Override
    public String getName() {
        return "Extra Life";
    }
}
