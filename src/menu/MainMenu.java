package menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame {
    private JButton newGameButton, highScoresButton, exitButton;

    public MainMenu() {
        initializeMenu();
    }

    private void initializeMenu() {
        setTitle("Pacman - Main Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        newGameButton = new JButton("New Game");
        highScoresButton = new JButton("High Scores");
        exitButton = new JButton("Exit");

        newGameButton.addActionListener(e -> {
            new BoardSelection();
            dispose();
        });

        highScoresButton.addActionListener(e -> {
            new HighScores();
            dispose();
        });

        exitButton.addActionListener(e -> System.exit(0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 0, 20, 0);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridy = 0;
        add(newGameButton, gbc);
        gbc.gridy = 1;
        add(highScoresButton, gbc);
        gbc.gridy = 2;
        add(exitButton, gbc);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeButtons();
            }
        });

        resizeButtons();
        setVisible(true);
    }

    private void resizeButtons() {
        int width = getWidth();
        int height = getHeight();

        int btnWidth = (int) (width * 0.3);
        int btnHeight = (int) (height * 0.1);
        Dimension size = new Dimension(btnWidth, btnHeight);

        newGameButton.setPreferredSize(size);
        highScoresButton.setPreferredSize(size);
        exitButton.setPreferredSize(size);

        SwingUtilities.invokeLater(() -> {
            getContentPane().invalidate();
            getContentPane().validate();
            getContentPane().repaint();
        });
    }

}
