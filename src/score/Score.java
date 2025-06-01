package score;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Score implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private final int id;
    private final String playerName;
    private final LocalDateTime date;
    private final int timePlayed;
    private final int value;

    public Score(int id, String playerName, LocalDateTime date, int timePlayed, int value) {
        this.id = id;
        this.playerName = playerName;
        this.date = date;
        this.timePlayed = timePlayed;
        this.value = value;
    }
    

    public int getValue() {
        return value;
    }

    public int getId() {
        return id;
    }

    public int getTimePlayed() {
        return timePlayed;
    }
    
    /**
     * Gets the player's name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Gets the date when the score was achieved
     */
    public LocalDateTime getDate() {
        return date;
    }
    
    @Override
    public String toString() {
        return String.format("Player: %s | Score: %d | Time: %ds | Date: %s",
                playerName, value, timePlayed, date.toLocalDate());
    }
}
