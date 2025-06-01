package game.powerups;

import game.Player;

/**
 * Double score power-up that doubles all points earned
 */
public class DoubleScore extends PowerUp {
    
    // Score multiplier
    private static final int SCORE_MULTIPLIER = 2;
    
    /**
     * Creates a new double score power-up
     * 
     * @param row The row position
     * @param col The column position
     */
    public DoubleScore(int row, int col) {
        super(row, col, 15000, "/assets/upgrades/double_score.png"); // 15 seconds duration
    }
    
    @Override
    public boolean applyEffect(Player player) {
        // Set the score multiplier
        player.setScoreMultiplier(SCORE_MULTIPLIER);
        return true;
    }
    
    @Override
    public void removeEffect(Player player) {
        // Reset the score multiplier
        player.setScoreMultiplier(1);
    }
    
    @Override
    public String getName() {
        return "Double Score";
    }
}
