package menu;

import javax.swing.*;
import java.awt.*;

public class BoardSelection extends JFrame {
    public BoardSelection() {
        setTitle("Select Board Size");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JLabel label = new JLabel("Board selection window", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

        setVisible(true);
    }
}
