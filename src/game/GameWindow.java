package game;

import board.Board;
import game.pacman.Pacman;
import menu.MainMenu;
import menu.PauseMenu;
import score.ScoreManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class GameWindow extends JFrame {
    private final Board board;
    private GameLoop gameLoop;
    private GamePanel gamePanel;

    private JLabel scoreLabel;
    private JLabel timerLabel;
    private JPanel livesPanel;

    private ImageIcon lifeIcon;

    private int score = 0;
    private boolean isPaused = false;
    private long startTime;

    public GameWindow(Board board) {
        this.board = board;
        setTitle("Pacman Game");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        initializeComponents();
        setupKeyboardControls();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        gameLoop.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (gameLoop != null) gameLoop.stop();
                new MainMenu();
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

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        infoPanel.setBackground(Color.BLACK);

        scoreLabel = createLabel("SCORE: 0");
        timerLabel = createLabel("TIME: 00:00");

        infoPanel.add(scoreLabel);
        infoPanel.add(timerLabel);
        infoPanel.add(createLivesDisplay());

        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        gamePanel = new GamePanel(board);
        mainPanel.add(gamePanel, BorderLayout.CENTER);

        gameLoop = new GameLoop(board, gamePanel);
        gameLoop.setGameWindow(this);

        Pacman pacman = gameLoop.getPlayer();
        pacman.setGamePanel(gamePanel);

        loadLifeIcon();
        updateLivesDisplay(pacman.getLives());

        startTime = System.currentTimeMillis();
        new Thread(this::runTimer).start();

        add(mainPanel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel createLivesDisplay() {
        JPanel livesContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        livesContainer.setBackground(Color.BLACK);

        JLabel livesText = createLabel("LIVES: ");
        livesContainer.add(livesText);

        livesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        livesPanel.setBackground(Color.BLACK);
        livesContainer.add(livesPanel);

        return livesContainer;
    }

    private void loadLifeIcon() {
        try {
            Image img = ImageIO.read(getClass().getResourceAsStream("/assets/upgrades/health.png"));
            lifeIcon = new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            throw new RuntimeException("Could not load life icon image", e);
        }
    }

    public void updateLivesDisplay(int lives) {
        livesPanel.removeAll();

        for (int i = 0; i < lives; i++) {
            livesPanel.add(new JLabel(lifeIcon));
        }

        livesPanel.revalidate();
        livesPanel.repaint();
    }

    private void runTimer() {
        while (true) {
            if (!isPaused) {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                String time = String.format("TIME: %02d:%02d", elapsed / 60, elapsed % 60);
                SwingUtilities.invokeLater(() -> timerLabel.setText(time));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void setupKeyboardControls() {
        InputMap input = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getRootPane().getActionMap();

        bindKey(input, actions, KeyEvent.VK_UP, "up", () -> gameLoop.handleInput(Pacman.UP));
        bindKey(input, actions, KeyEvent.VK_W, "up", () -> gameLoop.handleInput(Pacman.UP));
        bindKey(input, actions, KeyEvent.VK_RIGHT, "right", () -> gameLoop.handleInput(Pacman.RIGHT));
        bindKey(input, actions, KeyEvent.VK_D, "right", () -> gameLoop.handleInput(Pacman.RIGHT));
        bindKey(input, actions, KeyEvent.VK_DOWN, "down", () -> gameLoop.handleInput(Pacman.DOWN));
        bindKey(input, actions, KeyEvent.VK_S, "down", () -> gameLoop.handleInput(Pacman.DOWN));
        bindKey(input, actions, KeyEvent.VK_LEFT, "left", () -> gameLoop.handleInput(Pacman.LEFT));
        bindKey(input, actions, KeyEvent.VK_A, "left", () -> gameLoop.handleInput(Pacman.LEFT));
        bindKey(input, actions, KeyEvent.VK_ESCAPE, "pause", this::togglePause);
        bindKey(input, actions, KeyEvent.VK_P, "pause", this::togglePause);

        setFocusable(true);
        requestFocusInWindow();
    }

    private void bindKey(InputMap map, ActionMap actions, int keyCode, String name, Runnable action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, 0);
        map.put(keyStroke, name);
        actions.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
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
        isPaused = true;
        gameLoop.pause();
        new PauseMenu(this);
    }

    public void resumeGame() {
        isPaused = false;
        gameLoop.resume();
        requestFocusInWindow();
    }

    public void showGameOver() {
        long endTime = System.currentTimeMillis();
        int duration = (int) ((endTime - startTime) / 1000);

        ScoreManager.addScore(score, duration);
        exitToMainMenu();
    }

    public void exitToMainMenu() {
        dispose();
    }
}