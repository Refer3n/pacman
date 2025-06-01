package game;

import board.Board;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
    
    public GameWindow(Board board, boolean returnToMainMenu) {
        this(board);
        this.returnToMainMenu = returnToMainMenu;
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
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        scoreLabel = new JLabel("SCORE: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setForeground(Color.WHITE);
        infoPanel.add(scoreLabel);
        
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        gamePanel = new GamePanel(board);
        mainPanel.add(gamePanel, BorderLayout.CENTER);

        gameLoop = new GameLoop(board, gamePanel);

        Player player = gameLoop.getPlayer();
        if (player != null) {
            player.setGamePanel(gamePanel);
        }
        
        add(mainPanel);
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
                    gameLoop.handleInput(Player.UP);
                }
            });
            
            actionMap.put(rightAction, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    gameLoop.handleInput(Player.RIGHT);
                }
            });
            
            actionMap.put(downAction, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    gameLoop.handleInput(Player.DOWN);
                }
            });
            
            actionMap.put(leftAction, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    gameLoop.handleInput(Player.LEFT);
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
}
