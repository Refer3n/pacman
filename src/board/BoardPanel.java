package board;

import game.upgrades.Upgrade;

import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {
    private final Board board;

    private final JPanel[][] cellPanels;

    public BoardPanel(Board board) {
        this.board = board;
        setLayout(new GridLayout(board.getHeight(), board.getWidth(), 0, 0));
        setBackground(Color.BLACK);
        setBorder(null);

        cellPanels = new JPanel[board.getHeight()][board.getWidth()];
        
        initializeBoardComponents();
    }

    private void initializeBoardComponents() {
        char[][] layout = board.getLayout();

        boolean[][] isWall = new boolean[board.getHeight()][board.getWidth()];
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                isWall[row][col] = (layout[row][col] == '|' || layout[row][col] == 'X');
            }
        }

        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                JPanel cellPanel;
                char tile = layout[row][col];

                if (tile == '|') {
                    boolean hasTopWall = (row > 0 && isWall[row-1][col]);
                    boolean hasRightWall = (col < board.getWidth()-1 && isWall[row][col+1]);
                    boolean hasBottomWall = (row < board.getHeight()-1 && isWall[row+1][col]);
                    boolean hasLeftWall = (col > 0 && isWall[row][col-1]);

                    cellPanel = new WallPanel(hasTopWall, hasRightWall, hasBottomWall, hasLeftWall);
                }
                else if (tile == 'X') {
                    Color orangeColor = new Color(255, 140, 0);
                    cellPanel = new WallPanel(false, true, false, true, orangeColor);
                }
                else if (tile == '.') {
                    cellPanel = new JPanel(new BorderLayout(0, 0));
                    cellPanel.setBackground(Color.BLACK);

                    JLabel dot = getDotLabel();
                    cellPanel.add(dot, BorderLayout.CENTER);
                }
                else {
                    cellPanel = new JPanel(new BorderLayout(0, 0));
                    cellPanel.setBackground(Color.BLACK);
                }

                cellPanel.setBorder(null);

                cellPanels[row][col] = cellPanel;

                add(cellPanel);
            }
        }
    }

    public void clearDot(int row, int col) {
        if (row >= 0 && row < board.getHeight() && col >= 0 && col < board.getWidth()) {
            JPanel cellPanel = cellPanels[row][col];
            
            if (cellPanel != null) {
                cellPanel.removeAll();

                cellPanel.setBackground(Color.BLACK);

                cellPanel.revalidate();
                cellPanel.repaint();
            }
        }
    }

    public void addUpgrade (int row, int col, Upgrade upgrade) {
        if (row >= 0 && row < board.getHeight() && col >= 0 && col < board.getWidth()) {
            JPanel cellPanel = cellPanels[row][col];
            
            if (cellPanel != null) {
                cellPanel.removeAll();

                JLabel upgradeLabel = new JLabel(upgrade.getIcon());
                upgradeLabel.setHorizontalAlignment(SwingConstants.CENTER);
                upgradeLabel.setVerticalAlignment(SwingConstants.CENTER);
                
                cellPanel.setLayout(new BorderLayout());
                cellPanel.add(upgradeLabel, BorderLayout.CENTER);
                
                cellPanel.revalidate();
                cellPanel.repaint();
            }
        }
    }

    public void removeUpgrade(int row, int col) {
        clearDot(row, col);
    }

    private JLabel getDotLabel() {
        JLabel dot = new JLabel("•");
        dot.setName("dot");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dot.setForeground(Color.WHITE);
        dot.setHorizontalAlignment(SwingConstants.CENTER);
        dot.setVerticalAlignment(SwingConstants.CENTER);
        return dot;
    }
}