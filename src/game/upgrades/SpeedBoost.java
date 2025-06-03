package game.upgrades;

import game.pacman.Pacman;

/**
 * Speed boost power-up that increases player movement speed
 */
public class SpeedBoost extends Upgrade {
    
    // Speed multiplier
    private static final float SPEED_MULTIPLIER = 1.5f;
    
    /**
     * Creates a new speed boost power-up
     * 
     * @param row The row position
     * @param col The column position
     */
    public SpeedBoost(int row, int col) {
        super(row, col, 10000, "/assets/upgrades/speed.png"); // 10 seconds duration
    }
    
    @Override
    public boolean applyEffect(Pacman pacman) {
        // Increase the player's speed
        pacman.setSpeedMultiplier(SPEED_MULTIPLIER);
        return true;
    }
    
    @Override
    public void removeEffect(Pacman pacman) {
        // Reset the player's speed
        pacman.setSpeedMultiplier(1.0f);
    }
    
    @Override
    public String getName() {
        return "Speed Boost";
    }
}
