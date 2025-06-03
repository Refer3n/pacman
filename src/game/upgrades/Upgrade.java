package game.upgrades;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.IOException;
import java.util.Objects;

import game.GamePanel;
import game.pacman.Pacman;

/**
 * Abstract base class for all power-ups in the game
 */
public abstract class Upgrade {
    
    // Position of the power-up
    protected int row;
    protected int col;
    
    // Duration of the power-up effect in milliseconds
    protected long duration;
    
    // Whether the power-up has been collected
    protected boolean collected = false;
    
    // The image icon for this power-up
    protected ImageIcon icon;
    
    // Size of the power-up icon
    protected static final int DEFAULT_SIZE = GamePanel.CELL_SIZE - 8;
    
    /**
     * Creates a new power-up
     * 
     * @param row The row position
     * @param col The column position
     * @param duration How long the power-up effect lasts (in milliseconds), 0 for instant effect
     * @param iconPath Path to the power-up icon in assets
     */
    public Upgrade(int row, int col, long duration, String iconPath) {
        this.row = row;
        this.col = col;
        this.duration = duration;
        
        // Load the icon
        try {
            Image img = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(iconPath)));
            this.icon = new ImageIcon(img.getScaledInstance(DEFAULT_SIZE, DEFAULT_SIZE, Image.SCALE_SMOOTH));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a default icon if the image couldn't be loaded
     */
    public abstract boolean applyEffect(Pacman pacman);
    
    /**
     * Remove the power-up effect from the player
     * 
     * @param pacman The player to remove the effect from
     */
    public abstract void removeEffect(Pacman pacman);
    
    /**
     * Get the row position
     */
    public int getRow() {
        return row;
    }
    
    /**
     * Get the column position
     */
    public int getCol() {
        return col;
    }
    
    /**
     * Get the duration of the power-up effect
     */
    public long getDuration() {
        return duration;
    }
    
    /**
     * Check if the power-up has been collected
     */
    public boolean isCollected() {
        return collected;
    }
    
    /**
     * Mark the power-up as collected
     */
    public void setCollected(boolean collected) {
        this.collected = collected;
    }
    
    /**
     * Get the power-up icon
     */
    public ImageIcon getIcon() {
        return icon;
    }
    
    /**
     * Get a descriptive name for this power-up
     */
    public abstract String getName();
}
