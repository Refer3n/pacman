package utils;

import javax.swing.*;
import java.awt.*;

public class UIUtils {
    public static JButton createStyledButton(String text, int fontSize) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        btn.setBackground(Color.BLACK);
        btn.setForeground(new Color(255, 200, 0));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));

        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(30, 30, 30));
                btn.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.BLACK);
                btn.repaint();
            }
        });

        return btn;
    }
}