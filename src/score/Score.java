package score;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public record Score(int id, String playerName, LocalDateTime date, int timePlayed, int value) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String playerName() {
        return playerName;
    }

    @Override
    public String toString() {
        return String.format("Player: %s | Score: %d | Time: %ds | Date: %s",
                playerName, value, timePlayed, date.toLocalDate());
    }
}
