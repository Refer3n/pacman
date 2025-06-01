package game.upgrades;

import game.Player;

public class DoubleScore extends Upgrade {

    private static final int SCORE_MULTIPLIER = 2;

    public DoubleScore(int row, int col) {
        super(row, col, 15000, "/assets/upgrades/double_score.png");
    }
    
    @Override
    public boolean applyEffect(Player player) {
        player.setScoreMultiplier(SCORE_MULTIPLIER);
        return true;
    }
    
    @Override
    public void removeEffect(Player player) {
        player.setScoreMultiplier(1);
    }
    
    @Override
    public String getName() {
        return "Double Score";
    }
}
