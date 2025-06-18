package app;

import view.SplashScreenUI;

import javax.swing.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Replace with call to DAO to fetch real profiles
        SwingUtilities.invokeLater(() -> new SplashScreenUI(List.of("Alice Smith", "Bob Dylan", "Charlie Ross")).setVisible(true));
        System.out.println("Setup complete.");
    }
}
