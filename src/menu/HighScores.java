package menu;

import score.Score;
import score.ScoreManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class HighScores extends JFrame {
    private boolean redirectToMenu = true;
    private final Color GOLD = new Color(255, 200, 0);
    private JList<Score> scoresList;
    private DefaultListModel<Score> scoresModel;
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
                "Scores",
                0, 0,
                new Font("SansSerif", Font.BOLD, 18),
                GOLD
        ));
        
        JLabel noScores = new JLabel("No scores available.");
        noScores.setForeground(GOLD);
        noScores.setFont(new Font("SansSerif", Font.PLAIN, 20));
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
        
        // Create list model and populate it
        scoresModel = new DefaultListModel<>();
        for (Score score : scores) {
            scoresModel.addElement(score);
        }
        
        // Create JList with the model
        scoresList = new JList<>(scoresModel);
        scoresList.setBackground(Color.BLACK);
        scoresList.setForeground(GOLD);
        scoresList.setFont(new Font("SansSerif", Font.PLAIN, 18));
        scoresList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scoresList.setFixedCellHeight(30); // Fixed height for each item
        
        // Custom renderer for better looking list items
        scoresList.setCellRenderer(new ScoreListCellRenderer());
        
        // Add the list to a scroll pane
        JScrollPane scrollPane = new JScrollPane(scoresList);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD),
                "Scores",
                0, 0,
                new Font("SansSerif", Font.BOLD, 18),
                GOLD
        ));
        
        // Create a delete button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.BLACK);
        
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setBackground(new Color(180, 40, 40));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        deleteButton.addActionListener(e -> deleteSelectedScore());
        
        buttonPanel.add(deleteButton);
        
        // Add components to panel
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return listPanel;
    }
    
    private void deleteSelectedScore() {
        int selectedIndex = scoresList.getSelectedIndex();
        if (selectedIndex >= 0) {
            Score selectedScore = scoresModel.getElementAt(selectedIndex);
            
            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this score?\n" + selectedScore,
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Delete from model and storage
                ScoreManager.deleteScore(selectedScore.getId());
                scoresModel.remove(selectedIndex);
                
                // Update stats
                updateStats();
                
                // If all scores deleted, rebuild UI
                if (scoresModel.isEmpty()) {
                    getContentPane().removeAll();
                    initialize();
                    revalidate();
                    repaint();
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Please select a score to delete.",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
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
    
    /**
     * Custom cell renderer for Score items in the JList
     */
    private class ScoreListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            
            // Configure appearance
            label.setBorder(new EmptyBorder(5, 10, 5, 10));
            
            if (value instanceof Score) {
                Score score = (Score) value;
                label.setText(score.toString());
            }
            
            if (isSelected) {
                label.setBackground(new Color(60, 60, 120));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.BLACK);
                label.setForeground(GOLD);
            }
            
            return label;
        }
    }
}