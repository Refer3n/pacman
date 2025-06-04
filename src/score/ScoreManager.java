package score;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import javax.swing.*;

public class ScoreManager {
    private static final String SCORES_DIR = "scores";
    private static final String SCORES_FILE = SCORES_DIR + "/pacman_scores.ser";
    private static int nextId = 1;
    private static Score bestScore = null;

    public static void addScore(int scoreValue, int timePlayedSeconds) {
        String playerName = PlayerNameDialog(scoreValue);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }
        
        Score score = new Score(nextId++, playerName, LocalDateTime.now(), timePlayedSeconds, scoreValue);
        saveScore(score);
        updateBestScore(score);
    }

    private static String PlayerNameDialog(int scoreValue) {
        return JOptionPane.showInputDialog(null, 
                "Enter your name for the high score:", 
                "Save Score, " +scoreValue + " points",
                JOptionPane.PLAIN_MESSAGE);
    }

    public static List<Score> getAllScores() {
        List<Score> scores = new ArrayList<>();

        File directory = new File(SCORES_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        File file = new File(SCORES_FILE);
        if (!file.exists()) {
            return scores;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORES_FILE))) {
            List<Score> loadedScores = (List<Score>) ois.readObject();
            scores.addAll(loadedScores);

            for (Score score : scores) {
                updateBestScore(score);
                nextId = Math.max(nextId, score.id() + 1);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading scores: " + e.getMessage());
            e.printStackTrace();
        }
        
        return scores;
    }

    public static int getTotalGames() {
        return getAllScores().size();
    }

    public static int getTotalScore() {
        return getAllScores().stream().mapToInt(Score::value).sum();
    }

    public static int getTotalPlayTimeSeconds() {
        return getAllScores().stream().mapToInt(Score::timePlayed).sum();
    }

    public static String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d min %02d sec", mins, secs);
    }

    private static void updateBestScore(Score score) {
        if (bestScore == null || score.value() > bestScore.value()) {
            bestScore = score;
        }
    }

    private static void saveScore(Score score) {
        List<Score> scores = getAllScores();
        scores.add(score);
        saveAllScores(scores);
    }

    private static void saveAllScores(List<Score> scores) {
        File directory = new File(SCORES_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORES_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

