package game;

import board.Board;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import game.pacman.Pacman;
import score.ScoreManager;
import java.io.IOException;

import menu.MainMenu;
import menu.PauseMenu;

public class GameWindow extends JFrame {
    private final Board board;
    private boolean returnToMainMenu = true;
    private GameLoop gameLoop;
    private GamePanel gamePanel;
    private JLabel scoreLabel;
    private int score = 0;
    private boolean isPaused = false;

    private JPanel livesPanel;
    private JLabel timerLabel;
    private long startTime;
    private ImageIcon lifeIcon;
    
    public GameWindow(Board board) {
        this.board = board;
        
        setTitle("Pacman Game");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupKeyboardControls();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (gamePanel != null) {
                    gamePanel.revalidate();
                    gamePanel.repaint();
                }
            }
        });
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        gameLoop.start();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (gameLoop != null) {
                    gameLoop.stop();
                }
                
                if (returnToMainMenu) {
                    new MainMenu();
                }
            }
        });
    }
    
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    
        JLabel titleLabel = new JLabel("PAC-MAN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
    
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.BLACK);
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
    
        // Score display
        scoreLabel = new JLabel("SCORE: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setForeground(Color.WHITE);
        infoPanel.add(scoreLabel);
        
        // Timer display
        timerLabel = new JLabel("TIME: 00:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(Color.WHITE);
        infoPanel.add(timerLabel);
        
        // Lives display
        JPanel livesContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        livesContainer.setBackground(Color.BLACK);
        
        JLabel livesTextLabel = new JLabel("LIVES: ");
        livesTextLabel.setFont(new Font("Arial", Font.BOLD, 16));
        livesTextLabel.setForeground(Color.WHITE);
        livesContainer.add(livesTextLabel);
        
        livesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        livesPanel.setBackground(Color.BLACK);
        livesContainer.add(livesPanel);

        Image img = null;
        try {
            img = ImageIO.read(getClass().getResourceAsStream("/assets/upgrades/health.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lifeIcon = new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        
        infoPanel.add(livesContainer);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
    
        gamePanel = new GamePanel(board);
        mainPanel.add(gamePanel, BorderLayout.CENTER);
    
        gameLoop = new GameLoop(board, gamePanel);
        // Set reference back to this window for score updates
        gameLoop.setGameWindow(this);
    
        Pacman pacman = gameLoop.getPlayer();
        if (pacman != null) {
            pacman.setGamePanel(gamePanel);
            updateLivesDisplay(pacman.getLives());
        }
        
        // Initialize start time for timer
        startTime = System.currentTimeMillis();
        
        // Start a timer to update the display
        new Thread(this::runTimer).start();
        
        add(mainPanel);
    }
    
    /**
     * Updates the lives display with the current number of player lives
     */
    public void updateLivesDisplay(int lives) {
        if (livesPanel != null) {
            livesPanel.removeAll();
            
            for (int i = 0; i < lives; i++) {
                JLabel lifeLabel = new JLabel(lifeIcon);
                livesPanel.add(lifeLabel);
            }
            
            livesPanel.revalidate();
            livesPanel.repaint();
        }
    }

    private void runTimer() {
        while (true) {
            if (!isPaused) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = (currentTime - startTime) / 1000;
                
                int minutes = (int) (elapsedTime / 60);
                int seconds = (int) (elapsedTime % 60);
                
                String timeString = String.format("TIME: %02d:%02d", minutes, seconds);
                
                SwingUtilities.invokeLater(() -> {
                    timerLabel.setText(timeString);
                });
            }
            
            try {
                Thread.sleep(1000); // Update every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void setupKeyboardControls() {
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = getRootPane().getInputMap(condition);
        ActionMap actionMap = getRootPane().getActionMap();

        String upAction = "move_up";
        String rightAction = "move_right";
        String downAction = "move_down";
        String leftAction = "move_left";
            String pauseAction = "pause_game";
    
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), upAction);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), upAction);
            
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), rightAction);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), rightAction);
            
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), downAction);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), downAction);
            
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), leftAction);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), leftAction);
            
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), pauseAction);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), pauseAction);
    
            actionMap.put(upAction, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    gameLoop.handleInput(Pacman.UP);
                }
            });
            
            actionMap.put(rightAction, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    gameLoop.handleInput(Pacman.RIGHT);
                }
            });
            
            actionMap.put(downAction, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    gameLoop.handleInput(Pacman.DOWN);
                }
            });
            
            actionMap.put(leftAction, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    gameLoop.handleInput(Pacman.LEFT);
                }
            });
            
            actionMap.put(pauseAction, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    togglePause();
                }
            });

        setFocusable(true);
        requestFocusInWindow();
    }

    public void updateScore(int points) {
        score += points;
        scoreLabel.setText("SCORE: " + score);
    }
    

    public void togglePause() {
        if (isPaused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    private void pauseGame() {
        if (!isPaused) {
            isPaused = true;
            gameLoop.pause();
            new PauseMenu(this);
        }
    }

    public void resumeGame() {
        if (isPaused) {
            isPaused = false;
            gameLoop.resume();
            requestFocusInWindow();
        }
    }

    public void exitToMainMenu() {
        returnToMainMenu = true;
        dispose();
    }
    
    /**
     * Handles game over, saves score to ScoreManager
     */
    public void showGameOver() {
        // Calculate total time played in seconds
        long endTime = System.currentTimeMillis();
        int timePlayedSeconds = (int)((endTime - startTime) / 1000);
        
        // Add score to the ScoreManager
        ScoreManager.addScore(score, timePlayedSeconds);
        
        // Return to main menu
        exitToMainMenu();
    }
}
