package game.upgrades;

import game.pacman.Pacman;

/**
 * Ghost killer power-up that allows the player to eat ghosts
 */
public class GhostKiller extends Upgrade {
    
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
    public boolean applyEffect(Pacman pacman) {
        // Enable ghost-eating mode
        pacman.setGhostKillerMode(true);
        return true;
    }
    
    @Override
    public void removeEffect(Pacman pacman) {
        // Disable ghost-eating mode
        pacman.setGhostKillerMode(false);
    }
    
    @Override
    public String getName() {
        return "Ghost Killer";
    }
}
