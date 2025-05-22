package menu;

import javax.swing.*;
import java.awt.*;

public class HighScores extends JFrame {
    public HighScores() {
        setTitle("High Scores");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JLabel label = new JLabel("High scores window", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

        setVisible(true);
    }
}
