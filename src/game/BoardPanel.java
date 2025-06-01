package game;

import board.Board;
import game.upgrades.Upgrade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class BoardPanel extends JPanel {
    private static final int CELL_SIZE = 30;
    private final Board board;

    private JPanel[][] cellPanels;

    public BoardPanel(Board board) {
        this.board = board;
        setLayout(new GridLayout(board.getHeight(), board.getWidth(), 0, 0));
        setBackground(Color.BLACK);
        setBorder(null);

        cellPanels = new JPanel[board.getHeight()][board.getWidth()];
        
        initializeBoardComponents();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                revalidate();
                repaint();
            }
        });
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
                } else if (tile == 'X') {
                    Color orangeColor = new Color(255, 140, 0);
                    cellPanel = new WallPanel(false, true, false, true, orangeColor);
                } else if (tile == '.') {
                    cellPanel = new JPanel(new BorderLayout(0, 0));
                    cellPanel.setBackground(Color.BLACK);

                    JLabel dot = new JLabel("•");
                    dot.setName("dot");
                    dot.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    dot.setForeground(Color.WHITE);
                    dot.setHorizontalAlignment(SwingConstants.CENTER);
                    dot.setVerticalAlignment(SwingConstants.CENTER);
                    cellPanel.add(dot, BorderLayout.CENTER);
                } else {
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
    
    /**
     * Adds a power-up to the board
     * 
     * @param row The row position
     * @param col The column position
     * @param upgrade The power-up to add
     */
    public void addPowerUp(int row, int col, Upgrade upgrade) {
        if (row >= 0 && row < board.getHeight() && col >= 0 && col < board.getWidth()) {
            JPanel cellPanel = cellPanels[row][col];
            
            if (cellPanel != null) {
                // Clear any existing content
                cellPanel.removeAll();
                
                // Add the power-up icon
                JLabel powerUpLabel = new JLabel(upgrade.getIcon());
                powerUpLabel.setName("powerup");
                powerUpLabel.setHorizontalAlignment(SwingConstants.CENTER);
                powerUpLabel.setVerticalAlignment(SwingConstants.CENTER);
                
                cellPanel.setLayout(new BorderLayout());
                cellPanel.add(powerUpLabel, BorderLayout.CENTER);
                
                cellPanel.revalidate();
                cellPanel.repaint();
            }
        }
    }
    
    /**
     * Removes a power-up from the board
     * 
     * @param row The row position
     * @param col The column position
     */
    public void removeUpgrade(int row, int col) {
        clearDot(row, col); // Reuse the clearDot method
    }
    
    /**
     * Refreshes a tile based on the current board state
     * 
     * @param row The row position
     * @param col The column position
     */
    public void refreshTile(int row, int col) {
        if (row >= 0 && row < board.getHeight() && col >= 0 && col < board.getWidth()) {
            JPanel cellPanel = cellPanels[row][col];
            
            if (cellPanel != null) {
                // Clear any existing content
                cellPanel.removeAll();
                
                // Get the current tile type
                char tile = board.getTile(row, col);
                
                // If it's a dot, add the dot graphic
                if (tile == '.') {
                    JLabel dot = new JLabel("•");
                    dot.setName("dot");
                    dot.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    dot.setForeground(Color.WHITE);
                    dot.setHorizontalAlignment(SwingConstants.CENTER);
                    dot.setVerticalAlignment(SwingConstants.CENTER);
                    
                    cellPanel.setLayout(new BorderLayout());
                    cellPanel.add(dot, BorderLayout.CENTER);
                }
                
                cellPanel.setBackground(Color.BLACK);
                cellPanel.revalidate();
                cellPanel.repaint();
            }
        }
    }
}
