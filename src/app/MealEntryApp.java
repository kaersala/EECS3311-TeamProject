/*
 * Launches a Swing frame
 * Loads your MealEntryPanel
 * Handles window setup
 */
package app;

import view.MealEntryPanel;

import javax.swing.*;

public class MealEntryApp {
    public static void main(String[] args) {
        
        // Launch the Swing UI for meal entry
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Log a Meal");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new MealEntryPanel());
            frame.setVisible(true);
        });
    }
}