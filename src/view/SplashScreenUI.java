package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SplashScreenUI extends JFrame {
    private JComboBox<String> profileDropdown;
    private JButton loadBtn, newProfileBtn;
    private Runnable onProfileSelected; // Callback for when profile is selected

    public SplashScreenUI(List<String> profiles) {
        this(profiles, null);
    }

    public SplashScreenUI(List<String> profiles, Runnable onProfileSelected) {
        this.onProfileSelected = onProfileSelected;
        setTitle("NutriSci - Select Profile");
        setSize(400, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel welcomeLabel = new JLabel("Welcome to NutriSci!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        profileDropdown = new JComboBox<>(profiles.toArray(new String[0]));

        loadBtn = new JButton("Select Profile");
        newProfileBtn = new JButton("Create New Profile");

        loadBtn.addActionListener(this::handleLoadProfile);
        newProfileBtn.addActionListener(e -> {
            JFrame createFrame = new JFrame("Create New Profile");
            createFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            createFrame.add(new CreateProfilePanel());
            createFrame.setSize(400, 400);
            createFrame.setLocationRelativeTo(null); // Center on screen
            createFrame.setVisible(true);
        });

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.add(new JLabel("Select a user profile:"));
        center.add(profileDropdown);
        center.add(loadBtn);

        JPanel bottom = new JPanel();
        bottom.add(newProfileBtn);

        setLayout(new BorderLayout(10, 10));
        add(welcomeLabel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void handleLoadProfile(ActionEvent e) {
        String selectedProfile = (String) profileDropdown.getSelectedItem();
        if (selectedProfile == null) {
            JOptionPane.showMessageDialog(this, "Please select a profile.");
            return;
        }

        // Show loading message
        JOptionPane.showMessageDialog(this, "Loading profile: " + selectedProfile);
        
        // Close this window
        this.dispose();
        
        // Call the callback to continue to next step
        if (onProfileSelected != null) {
            onProfileSelected.run();
        }
    }

    // For testing
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            List<String> mockProfiles = List.of("Alice Smith", "Bob Johnson", "Charlie Lee");
//            new SplashScreenUI(mockProfiles).setVisible(true);
//        });
//    }
}

