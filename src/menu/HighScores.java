package menu;

import score.Score;
import score.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class HighScores extends JFrame {
    private boolean redirectToMenu = true;
    private final Color GOLD = new Color(255, 200, 0);
    private JPanel statsPanel;

    public HighScores() {
        setTitle("High Scores");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 400));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        initialize();

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (redirectToMenu) new MainMenu();
            }
        });
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create scores section with JList
        List<Score> allScores = ScoreManager.getAllScores();
        
        if (allScores.isEmpty()) {
            mainPanel.add(createEmptyScoresPanel(), BorderLayout.CENTER);
        } else {
            mainPanel.add(createScoresListPanel(allScores), BorderLayout.CENTER);
        }

        // Create statistics panel
        statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statsPanel.setBackground(Color.BLACK);
        statsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD),
                "Statistics",
                0, 0,
                new Font("SansSerif", Font.BOLD, 18),
                GOLD
        ));

        updateStats();
        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
    
    private JPanel createEmptyScoresPanel() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBackground(Color.BLACK);
        emptyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD),
                "High Scores",
                0, 0,
                new Font("SansSerif", Font.BOLD, 18),
                GOLD
        ));
        
        JLabel noScores = new JLabel("No scores available yet. Play a game to set a high score!");
        noScores.setForeground(GOLD);
        noScores.setFont(new Font("SansSerif", Font.PLAIN, 18));
        noScores.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton playBtn = new JButton("Play now", new ImageIcon(getClass().getResource("/assets/icons/play-icon.png")));
        playBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        playBtn.setIconTextGap(10);
        playBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        playBtn.setBackground(new Color(180, 90, 30));
        playBtn.setForeground(Color.WHITE);
        playBtn.setFocusPainted(false);
        playBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        playBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        playBtn.setMaximumSize(new Dimension(200, 50));
        
        playBtn.addActionListener(e -> {
            redirectToMenu = false;
            dispose();
            new BoardSelection();
        });
        
        // Add components with some spacing
        emptyPanel.add(Box.createVerticalGlue());
        emptyPanel.add(noScores);
        emptyPanel.add(Box.createVerticalStrut(20));
        emptyPanel.add(playBtn);
        emptyPanel.add(Box.createVerticalGlue());
        
        return emptyPanel;
    }
    
    private JPanel createScoresListPanel(List<Score> scores) {
        JPanel listPanel = new JPanel(new BorderLayout(0, 10));
        listPanel.setBackground(Color.BLACK);
        
        // Create a simple list model and populate it
        DefaultListModel<String> listModel = new DefaultListModel<>();
        
        // Add header
        listModel.addElement(String.format("%-25s %-15s %-15s", "Name", "Score", "Time Played"));
        
        // Add a separator
        listModel.addElement("--------------------------------------------------");
        
        // Add all scores
        for (Score score : scores) {
            String formattedScore = String.format("%-25s %-15d %-15s", 
                score.playerName(),
                score.value(),
                ScoreManager.formatTime(score.timePlayed()));
            listModel.addElement(formattedScore);
        }
        
        // Create JList with the model - non-selectable
        JList<String> scoresList = new JList<>(listModel);
        scoresList.setBackground(Color.BLACK);
        scoresList.setForeground(GOLD);
        scoresList.setFont(new Font("Monospaced", Font.PLAIN, 16));
        scoresList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scoresList.setFixedCellHeight(30);
        scoresList.setEnabled(false); // Make it non-selectable
        
        // Add the list to a scroll pane
        JScrollPane scrollPane = new JScrollPane(scoresList);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD),
                "High Scores",
                0, 0,
                new Font("SansSerif", Font.BOLD, 18),
                GOLD
        ));
        
        // Add components to panel
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        return listPanel;
    }
    
    // Method removed
    
    private void updateStats() {
        if (statsPanel != null) {
            statsPanel.removeAll();
            
            addStatLabel(statsPanel, "Total Games: " + ScoreManager.getTotalGames());
            addStatLabel(statsPanel, "Total Score: " + ScoreManager.getTotalScore());
            addStatLabel(statsPanel, "Total Play Time: " + ScoreManager.formatTime(ScoreManager.getTotalPlayTimeSeconds()));
            
            statsPanel.revalidate();
            statsPanel.repaint();
        }
    }

    private void addStatLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(GOLD);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        panel.add(label);
    }
}