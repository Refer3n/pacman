package game.upgrades;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import board.Board;
import game.BoardPanel;
import game.GameWindow;
import game.Player;


public class UpgradeManager {

    private List<Upgrade> upgrades = new ArrayList<>();

    private Map<Upgrade, Long> activeEffects = new HashMap<>();
    
    private final Board board;
    private final Random random = new Random();
    private BoardPanel boardPanel;

    public UpgradeManager(Board board) {
        this.board = board;
    }

    public void setBoardPanel(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
    }

    public Upgrade createPowerUp(int row, int col) {
        for (Upgrade upgrade : upgrades) {
            if (!upgrade.isCollected() && upgrade.getRow() == row && upgrade.getCol() == col) {
                return null;
            }
        }
        
        // Create a random power-up
        Upgrade upgrade;
        int powerUpType = random.nextInt(4);
        
        switch (powerUpType) {
            case 0:
                upgrade = new SpeedBoost(row, col);
                break;
            case 1:
                upgrade = new DoubleScore(row, col);
                break;
            case 2:
                upgrade = new HealthRestore(row, col);
                break;
            case 3:
                upgrade = new GhostKiller(row, col);
                break;
            default:
                upgrade = new SpeedBoost(row, col);
        }
        
        // Add to list
        upgrades.add(upgrade);
        
        // Update the board panel with the new power-up
        if (boardPanel != null) {
            boardPanel.addPowerUp(row, col, upgrade);
        }
        
        return upgrade;
    }
    
    /**
     * Checks if the player has collected any power-ups
     * 
     * @param player The player
     * @return The collected power-up or null if none
     */
    public Upgrade checkPowerUpCollection(Player player) {
        int playerRow = player.getRow();
        int playerCol = player.getCol();
        Upgrade collectedUpgrade = null;
        
        for (Upgrade upgrade : upgrades) {
            if (!upgrade.isCollected() && upgrade.getRow() == playerRow && upgrade.getCol() == playerCol) {
                // Collect the power-up
                upgrade.setCollected(true);
                collectedUpgrade = upgrade;
                
                // Remove from board panel
                if (boardPanel != null) {
                    boardPanel.removeUpgrade(upgrade.getRow(), upgrade.getCol());
                }
                
                break;
            }
        }
        
        return collectedUpgrade;
    }
    
    /**
     * Activates a power-up effect
     * 
     * @param upgrade The power-up to activate
     * @param player The player to apply the effect to
     */
    public void activatePowerUp(Upgrade upgrade, Player player) {
        // Apply the effect
        if (upgrade.applyEffect(player)) {
            // If it has a duration, add to active effects
            if (upgrade.getDuration() > 0) {
                long expirationTime = System.currentTimeMillis() + upgrade.getDuration();
                activeEffects.put(upgrade, expirationTime);
            }
            
            // Notify about the power-up
            System.out.println("Activated " + upgrade.getName() + "!");
            
            // For health restore, update the lives display in the game window
            if (upgrade instanceof HealthRestore && boardPanel != null) {
                // Find the game window to update lives display
                Component parent = boardPanel;
                while (parent != null && !(parent instanceof GameWindow)) {
                    parent = parent.getParent();
                }
                
                if (parent != null) {
                    ((GameWindow) parent).updateLivesDisplay(player.getLives());
                }
            }
        }
    }
    
    public List<Upgrade> updateActiveEffects(Player player, long currentTime) {
        List<Upgrade> expiredEffects = new ArrayList<>();
        
        // Check for expired effects
        for (Map.Entry<Upgrade, Long> entry : new HashMap<>(activeEffects).entrySet()) {
            Upgrade upgrade = entry.getKey();
            long expirationTime = entry.getValue();
            
            if (currentTime >= expirationTime) {
                // Remove effect
                upgrade.removeEffect(player);
                activeEffects.remove(upgrade);
                expiredEffects.add(upgrade);
                
                System.out.println(upgrade.getName() + " effect expired");
            }
        }
        
        return expiredEffects;
    }
    
    /**
     * Removes all upgrades from the map and optionally replaces them with dots
     * 
     * @param fillWithDots Whether to replace the cleared spaces with dots
     * @return The number of upgrades removed
     */
    public int removeAllUpgrades(boolean fillWithDots) {
        int count = 0;
        
        // Create a copy of the list to avoid concurrent modification
        List<Upgrade> upgradesToRemove = new ArrayList<>(upgrades);
        
        for (Upgrade upgrade : upgradesToRemove) {
            if (!upgrade.isCollected()) {
                int row = upgrade.getRow();
                int col = upgrade.getCol();
                
                // Remove from board panel
                if (boardPanel != null) {
                    boardPanel.removeUpgrade(row, col);
                }
                
                // Optionally replace with a dot
                if (fillWithDots) {
                    board.updateTile(row, col, '.');
                    if (boardPanel != null) {
                        // The board panel needs to be refreshed to show the new dot
                        boardPanel.refreshTile(row, col);
                    }
                }
                
                count++;
            }
        }
        
        // Clear the upgrades list
        upgrades.clear();
        
        return count;
    }
}
