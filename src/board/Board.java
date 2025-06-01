package board;

public class Board {
    private final int width;
    private final int height;
    private final char[][] layout;
    
    public Board(char[][] layout) {
        this.height = layout.length;
        this.width = layout[0].length;
        this.layout = layout;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public char[][] getLayout() {
        return layout;
    }

    public void updateTile(int row, int col, char newTile) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            layout[row][col] = newTile;
        }
    }

    public char getTile(int row, int col) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            return layout[row][col];
        }
        return ' ';
    }
}

