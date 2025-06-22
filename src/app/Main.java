package app;

import view.SplashScreenUI;
import view.GoalSelectionUI;
import model.Goal;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
         //Replace with call to DAO to fetch real profiles
        //SwingUtilities.invokeLater(() -> new SplashScreenUI(List.of("Alice Smith", "Bob Dylan", "Charlie Ross")).setVisible(true));
        //System.out.println("Setup complete.");
    	
    	SwingUtilities.invokeLater(() -> {
            // Sample predefined goals (replace with real data or DAO later)
            List<Goal> goals = new ArrayList<>();
            goals.add(new Goal("Fiber", "Increase", 2.0, ""));
            goals.add(new Goal("Calories", "Decrease", 10.0, ""));
            goals.add(new Goal("Sodium", "Decrease", 1.5, ""));
            goals.add(new Goal("Protein", "Increase", 5.0, ""));

            // Set up and display the UI
            JFrame frame = new JFrame("Select Nutritional Goals");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new GoalSelectionUI(goals));
            frame.pack();
            frame.setLocationRelativeTo(null); // center window
            frame.setVisible(true);
        });
    }
    
    }
    

