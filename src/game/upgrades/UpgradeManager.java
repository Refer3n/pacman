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
import game.pacman.Pacman;


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

    public void createPowerUp(int row, int col) {
        for (Upgrade upgrade : upgrades) {
            if (!upgrade.isCollected() && upgrade.getRow() == row && upgrade.getCol() == col) {
                return;
            }
        }

        Upgrade upgrade;
        int powerUpType = random.nextInt(4);

        upgrade = switch (powerUpType) {
            case 0 -> new SpeedBoost(row, col);
            case 1 -> new DoubleScore(row, col);
            case 2 -> new HealthRestore(row, col);
            case 3 -> new GhostKiller(row, col);
            default -> new SpeedBoost(row, col);
        };

        upgrades.add(upgrade);

        boardPanel.addPowerUp(row, col, upgrade);
    }

    public Upgrade checkPowerUpCollection(Pacman pacman) {
        int playerRow = pacman.getRow();
        int playerCol = pacman.getCol();
        Upgrade collectedUpgrade = null;
        
        for (Upgrade upgrade : upgrades) {
            if (!upgrade.isCollected() && upgrade.getRow() == playerRow && upgrade.getCol() == playerCol) {
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

    public void activatePowerUp(Upgrade upgrade, Pacman pacman) {
        if (upgrade.applyEffect(pacman)) {
            if (upgrade.getDuration() > 0) {
                long expirationTime = System.currentTimeMillis() + upgrade.getDuration();
                activeEffects.put(upgrade, expirationTime);
            }

            if (upgrade instanceof HealthRestore && boardPanel != null) {
                Component parent = boardPanel;
                while (parent != null && !(parent instanceof GameWindow)) {
                    parent = parent.getParent();
                }
                
                if (parent != null) {
                    ((GameWindow) parent).updateLivesDisplay(pacman.getLives());
                }
            }
        }
    }
    
    public void updateActiveEffects(Pacman pacman, long currentTime) {
        for (Map.Entry<Upgrade, Long> entry : new HashMap<>(activeEffects).entrySet()) {
            Upgrade upgrade = entry.getKey();
            long expirationTime = entry.getValue();
            
            if (currentTime >= expirationTime) {
                upgrade.removeEffect(pacman);
                activeEffects.remove(upgrade);
            }
        }
    }

    public void removeAllUpgrades(boolean fillWithDots) {
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
                        boardPanel.refreshTile(row, col);
                    }
                }
            }
        }

        upgrades.clear();
    }
}
