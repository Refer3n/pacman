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
        String playerName = promptForPlayerName(scoreValue);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }
        
        Score score = new Score(nextId++, playerName, LocalDateTime.now(), timePlayedSeconds, scoreValue);
        saveScore(score);
        updateBestScore(score);
    }

    /**
     * Prompts the user to enter their name for the score
     * 
     * @return The player's name, or null if canceled
     */
    private static String promptForPlayerName(int scoreValue) {
        return JOptionPane.showInputDialog(null, 
                "Enter your name for the high score:", 
                "Save Score, " +scoreValue + " points",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Gets all saved scores
     * 
     * @return List of all scores
     */
    public static List<Score> getAllScores() {
        List<Score> scores = new ArrayList<>();
        
        // Create scores directory if it doesn't exist
        File directory = new File(SCORES_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        File file = new File(SCORES_FILE);
        if (!file.exists()) {
            return scores; // Empty list if no file
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORES_FILE))) {
            @SuppressWarnings("unchecked")
            List<Score> loadedScores = (List<Score>) ois.readObject();
            scores.addAll(loadedScores);
            
            // Update nextId and bestScore based on loaded scores
            for (Score score : scores) {
                updateBestScore(score);
                nextId = Math.max(nextId, score.getId() + 1);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading scores: " + e.getMessage());
            e.printStackTrace();
        }
        
        return scores;
    }

    /**
     * Deletes a score by ID
     * 
     * @param id The ID of the score to delete
     */
    public static void deleteScore(int id) {
        List<Score> scores = getAllScores();
        scores.removeIf(score -> score.getId() == id);
        saveAllScores(scores);
        refreshBestScore();
    }

    /**
     * Gets the highest score
     * 
     * @return The best score, or null if no scores exist
     */
    public static Score getBestScore() {
        if (bestScore == null) refreshBestScore();
        return bestScore;
    }

    /**
     * Gets the total number of games played
     * 
     * @return The total games count
     */
    public static int getTotalGames() {
        return getAllScores().size();
    }

    /**
     * Gets the sum of all scores
     * 
     * @return The total points earned across all games
     */
    public static int getTotalScore() {
        return getAllScores().stream().mapToInt(Score::getValue).sum();
    }

    /**
     * Gets the total time played across all games
     * 
     * @return The total play time in seconds
     */
    public static int getTotalPlayTimeSeconds() {
        return getAllScores().stream().mapToInt(Score::getTimePlayed).sum();
    }

    public static String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d min %02d sec", mins, secs);
    }

    private static void refreshBestScore() {
        bestScore = null;
        getAllScores().forEach(ScoreManager::updateBestScore);
    }


    private static void updateBestScore(Score score) {
        if (bestScore == null || score.getValue() > bestScore.getValue()) {
            bestScore = score;
        }
    }

    private static void saveScore(Score score) {
        List<Score> scores = getAllScores();
        scores.add(score);
        saveAllScores(scores);
    }

    private static void saveAllScores(List<Score> scores) {
        // Create scores directory if it doesn't exist
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

