package game.upgrades;

import game.pacman.Pacman;

public class DoubleScore extends Upgrade {

    private static final int SCORE_MULTIPLIER = 2;

    public DoubleScore(int row, int col) {
        super(row, col, 15000, "/assets/upgrades/double_score.png");
    }
    
    @Override
    public boolean applyEffect(Pacman pacman) {
        pacman.setScoreMultiplier(SCORE_MULTIPLIER);
        return true;
    }
    
    @Override
    public void removeEffect(Pacman pacman) {
        pacman.setScoreMultiplier(1);
    }
    
    @Override
    public String getName() {
        return "Double Score";
    }
}
