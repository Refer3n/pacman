package game;

import board.Board;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import game.ghosts.Ghost;
import game.pacman.Pacman;

public class GamePanel extends JLayeredPane {
    public static final int CELL_SIZE = 30;

    private final Board board;
    private Pacman pacman;
    private JLabel playerLabel;
    private BoardPanel boardPanel;

    private List<Ghost> ghosts = new ArrayList<>();
    private List<JLabel> ghostLabels = new ArrayList<>();

    private static final int BOARD_LAYER = 0;
    private static final int GHOST_LAYER = 1;
    private static final int PLAYER_LAYER = 2;

    public GamePanel(Board board) {
        this.board = board;

        setBackground(Color.BLACK);
        setOpaque(true);

        int width = board.getWidth() * CELL_SIZE;
        int height = board.getHeight() * CELL_SIZE;
        
        setPreferredSize(new Dimension(width, height));
        setSize(width, height);
        
        setLayout(null);
    
        initializeBoard();
    }
    
    private void initializeBoard() {
        boardPanel = new BoardPanel(board);
    
        int width = board.getWidth() * CELL_SIZE;
        int height = board.getHeight() * CELL_SIZE;
        boardPanel.setBounds(0, 0, width, height);
        add(boardPanel, Integer.valueOf(BOARD_LAYER));
    
        playerLabel = new JLabel();
        playerLabel.setBounds(0, 0, CELL_SIZE, CELL_SIZE);
        add(playerLabel, Integer.valueOf(PLAYER_LAYER));
    }

    public void setPlayer(Pacman pacman) {
        this.pacman = pacman;
        updatePlayerSprite();
        updatePlayerPosition();
    }

    public void addGhost(Ghost ghost) {
        ghosts.add(ghost);
        ghost.setGamePanel(this);

        JLabel ghostLabel = new JLabel();
        ghostLabel.setBounds(0, 0, CELL_SIZE, CELL_SIZE);
        add(ghostLabel, Integer.valueOf(GHOST_LAYER));
        ghostLabels.add(ghostLabel);

        ImageIcon icon = ghost.getCurrentIcon();
        if (icon != null) {
            ghostLabel.setIcon(icon);
        }

        int x = Math.round(ghost.getPixelX());
        int y = Math.round(ghost.getPixelY());
        ghostLabel.setLocation(x, y);
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    /**
     * Resets all ghosts to their starting positions
     */
    public void resetGhosts() {
        for (Ghost ghost : ghosts) {
            ghost.reset();
        }
        updateGhostPositions();
        updateGhostSprites();
    }

    public void updatePlayerPosition() {
        if (pacman != null && playerLabel != null) {
            int x = Math.round(pacman.getPixelX());
            int y = Math.round(pacman.getPixelY());
            playerLabel.setLocation(x, y);

            boardPanel.repaint();
            repaint();
        }
    }

    public void updateGhostPositions() {
        if (ghostLabels != null && !ghosts.isEmpty()) {
            for (int i = 0; i < ghosts.size(); i++) {
                Ghost ghost = ghosts.get(i);
                JLabel ghostLabel = ghostLabels.get(i);

                // Use pixel coordinates for smooth movement
                int x = Math.round(ghost.getPixelX());
                int y = Math.round(ghost.getPixelY());
                ghostLabel.setLocation(x, y);
            }

            boardPanel.repaint();
            repaint();
        }
    }

    public void updatePlayerSprite() {
        if (pacman != null && playerLabel != null) {
            ImageIcon icon = pacman.getCurrentIcon();
            if (icon != null) {
                playerLabel.setIcon(icon);
            }
        }
    }

    public void updateGhostSprites() {
        if (ghostLabels != null && !ghosts.isEmpty()) {
            for (int i = 0; i < ghosts.size(); i++) {
                Ghost ghost = ghosts.get(i);
                JLabel ghostLabel = ghostLabels.get(i);

                ImageIcon icon = ghost.getCurrentIcon();
                if (icon != null) {
                    ghostLabel.setIcon(icon);
                }
            }
        }
    }

    public void clearDot(int row, int col) {
        if (row >= 0 && row < board.getHeight() && col >= 0 && col < board.getWidth()) {
            boardPanel.clearDot(row, col);
        }
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }
}
