package menu;

import game.GameWindow;
import utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PauseMenu extends JDialog {
    private final GameWindow gameWindow;
    
    public PauseMenu(GameWindow gameWindow) {
        super(gameWindow, "Game Paused", true);
        this.gameWindow = gameWindow;
        
        setSize(300, 250);
        setResizable(false);
        setLocationRelativeTo(gameWindow);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        initializeComponents();

            KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            getRootPane().registerKeyboardAction(
                e -> resumeGame(),
                escapeKeyStroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
            
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resumeGame();
            }
        });
        
        setVisible(true);
    }
    
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        
        JLabel titleLabel = new JLabel("PAUSED", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(255, 200, 0));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.BLACK);
        
        JButton resumeButton = UIUtils.createStyledButton("Resume Game", 18);
        resumeButton.setPreferredSize(new Dimension(200, 40));
        resumeButton.setMaximumSize(new Dimension(200, 40));
        resumeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resumeButton.addActionListener(e -> resumeGame());

        JButton exitButton = UIUtils.createStyledButton("Exit to Main Menu", 18);
        exitButton.setPreferredSize(new Dimension(200, 40));
        exitButton.setMaximumSize(new Dimension(200, 40));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> exitToMainMenu());
        
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(resumeButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(exitButton);
        
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void resumeGame() {
        dispose();
        gameWindow.resumeGame();
    }
    
    private void exitToMainMenu() {
        dispose();
        gameWindow.exitToMainMenu();
    }
}
