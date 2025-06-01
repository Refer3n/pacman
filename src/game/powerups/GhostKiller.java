package game.powerups;

import game.Player;

/**
 * Ghost killer power-up that allows the player to eat ghosts
 */
public class GhostKiller extends PowerUp {
    
    /**
     * Creates a new ghost killer power-up
     * 
     * @param row The row position
     * @param col The column position
     */
    public GhostKiller(int row, int col) {
        super(row, col, 8000, "/assets/upgrades/ghost_killer.png"); // 8 seconds duration
    }
    
    @Override
    public boolean applyEffect(Player player) {
        // Enable ghost-eating mode
        player.setGhostKillerMode(true);
        return true;
    }
    
    @Override
    public void removeEffect(Player player) {
        // Disable ghost-eating mode
        player.setGhostKillerMode(false);
    }
    
    @Override
    public String getName() {
        return "Ghost Killer";
    }
}
