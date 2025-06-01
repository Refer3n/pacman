package menu;

import utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame {
    private JButton newGameButton, highScoresButton, exitButton;
    private JLabel titleLabel;

    public MainMenu()
    {
        setTitle("Pacman - Main Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setSize(800, 600);
        setLocationRelativeTo(null);

        initialize();

        setVisible(true);
    }

    private void initialize() {
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.BLACK);

        titleLabel = new JLabel("PAC-MAN");
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 60));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        newGameButton = UIUtils.createStyledButton("New Game", 28);
        highScoresButton = UIUtils.createStyledButton("High Scores", 28);
        exitButton = UIUtils.createStyledButton("Exit", 28);

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
        add(titleLabel, gbc);
        gbc.gridy = 1;
        add(newGameButton, gbc);
        gbc.gridy = 2;
        add(highScoresButton, gbc);
        gbc.gridy = 3;
        add(exitButton, gbc);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeButtons();
            }
        });

        resizeButtons();
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