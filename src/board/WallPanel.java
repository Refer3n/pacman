package board;

import javax.swing.*;
import java.awt.*;

public class WallPanel extends JPanel {
    private static final Color DEFAULT_WALL_COLOR = new Color(33, 33, 255);

    private final boolean hasTopWall;
    private final boolean hasRightWall;
    private final boolean hasBottomWall;
    private final boolean hasLeftWall;

    private final Color wallColor;

    public WallPanel(boolean hasTopWall, boolean hasRightWall,
                     boolean hasBottomWall, boolean hasLeftWall) {
        this(hasTopWall, hasRightWall, hasBottomWall, hasLeftWall, DEFAULT_WALL_COLOR);
    }

    public WallPanel(boolean hasTopWall, boolean hasRightWall,
                     boolean hasBottomWall, boolean hasLeftWall,
                     Color wallColor) {
        this.hasTopWall = hasTopWall;
        this.hasRightWall = hasRightWall;
        this.hasBottomWall = hasBottomWall;
        this.hasLeftWall = hasLeftWall;
        this.wallColor = wallColor;

        setBackground(Color.BLACK);
        setLayout(new GridLayout(3, 3, 0, 0));
        setBorder(BorderFactory.createEmptyBorder());

        buildWallGrid();
    }

    private void buildWallGrid() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createEmptyBorder());

                if (shouldPaintWall(row, col)) {
                    cell.setBackground(wallColor);
                } else {
                    cell.setBackground(Color.BLACK);
                }

                add(cell);
            }
        }
    }

    private boolean shouldPaintWall(int row, int col) {
        if (row == 1 && col == 1) return true;

        if (row == 0 && col == 1) return hasTopWall;
        if (row == 1 && col == 2) return hasRightWall;
        if (row == 2 && col == 1) return hasBottomWall;
        if (row == 1 && col == 0) return hasLeftWall;

        if (row == 0 && col == 0) return hasTopWall && hasLeftWall;
        if (row == 0 && col == 2) return hasTopWall && hasRightWall;
        if (row == 2 && col == 0) return hasBottomWall && hasLeftWall;
        if (row == 2 && col == 2) return hasBottomWall && hasRightWall;

        return false;
    }
}
