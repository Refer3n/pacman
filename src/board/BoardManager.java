package board;

import java.io.*;
import java.util.*;

public class BoardManager {
    private final Map<String, Board> boards = new HashMap<>();

    public BoardManager() {
        loadBoards();
    }

    private void loadBoards() {
        String[] files = {"small.txt"};

        for (String fileName : files) {
            try (InputStream is = getClass().getResourceAsStream("/assets/maps/" + fileName);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                String name = fileName.replace(".txt", "");
                List<String> lines = reader.lines().toList();
                char[][] layout = lines.stream().map(String::toCharArray).toArray(char[][]::new);
                boards.put(name, new Board(layout));

            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public Board getBoard(String name) {
        return boards.get(name);
    }

    public List<Board> getAllBoards() {
        return new ArrayList<>(boards.values());
    }
}
