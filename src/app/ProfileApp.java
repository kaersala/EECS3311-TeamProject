package app;

import view.CreateProfilePanel;

import javax.swing.*;

public class ProfileApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Create New Profile");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 350);
            frame.setContentPane(new CreateProfilePanel());
            frame.setLocationRelativeTo(null); // center on screen
            frame.setVisible(true);
        });
    }
}
