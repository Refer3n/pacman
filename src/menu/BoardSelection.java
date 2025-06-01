package menu;

import board.Board;
import board.BoardManager;
import game.GameWindow;
import utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BoardSelection extends JFrame {
    private final BoardManager boardManager;
    private boolean redirectToGame = false;

    public BoardSelection() {
        setTitle("Select Board Size");
        setSize(400, 360);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        boardManager = new BoardManager();
        initialize();

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!redirectToGame) {
                    new MainMenu();
                }
            }
        });
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.BLACK);

        JLabel label = new JLabel("Select Board Size", SwingConstants.CENTER);
        label.setForeground(new Color(255, 200, 0));
        label.setFont(new Font("SansSerif", Font.BOLD, 26));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(label);
        centerPanel.add(Box.createVerticalStrut(25));

        String[] buttonLabels = {"Small", "Medium", "Large"};
        for (String text : buttonLabels) {
            JButton btn = UIUtils.createStyledButton(text, 22);
            btn.setPreferredSize(new Dimension(240, 40));
            btn.setMaximumSize(new Dimension(240, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);

            btn.addActionListener(createBoardSelectionListener(text.toLowerCase()));
            
            centerPanel.add(btn);
            centerPanel.add(Box.createVerticalStrut(15));
        }

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
    }
    
    private ActionListener createBoardSelectionListener(String boardSize) {
        return e -> {
            Board selectedBoard = boardManager.getBoard(boardSize);
            if (selectedBoard != null) {
                redirectToGame = true;
                dispose();
                new GameWindow(selectedBoard);
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Board not available: " + boardSize,
                    "Board Selection Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        };
    }
}

