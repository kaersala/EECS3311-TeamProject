package view;

import service.UserProfileManager;
import model.user.UserProfile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SplashScreenUI extends JFrame {
    private JComboBox<String> profileDropdown;
    private JButton loadBtn, newProfileBtn;
    private Runnable onProfileSelected; // Callback for when profile is selected
    private UserProfileManager profileManager;

    public SplashScreenUI(Runnable onProfileSelected) {
        this.onProfileSelected = onProfileSelected;
        this.profileManager = UserProfileManager.getInstance();
        
        setTitle("NutriSci - Welcome");
        setSize(450, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel welcomeLabel = new JLabel("Welcome to NutriSci!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Load existing profiles
        List<UserProfile> profiles = profileManager.getProfiles();
        
        if (profiles.isEmpty()) {
            // No profiles exist - show create profile message
            showCreateProfileOnly();
        } else {
            // Profiles exist - show selection interface
            showProfileSelection(profiles);
        }
    }

    private void showCreateProfileOnly() {
        JLabel messageLabel = new JLabel("No user profiles found.");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel instructionLabel = new JLabel("Please create a new profile to get started.");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        newProfileBtn = new JButton("Create New Profile");
        newProfileBtn.setFont(new Font("Arial", Font.BOLD, 14));
        newProfileBtn.addActionListener(e -> createNewProfile());

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.add(messageLabel);
        center.add(instructionLabel);
        center.add(newProfileBtn);

        setLayout(new BorderLayout(20, 20));
        add(new JLabel("Welcome to NutriSci!"), BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void showProfileSelection(List<UserProfile> profiles) {
        // Convert profiles to display names (only show name, not ID)
        String[] profileNames = profiles.stream()
                .map(UserProfile::getName)
                .toArray(String[]::new);

        profileDropdown = new JComboBox<>(profileNames);
        profileDropdown.setFont(new Font("Arial", Font.PLAIN, 14));

        loadBtn = new JButton("Select Profile");
        loadBtn.setFont(new Font("Arial", Font.BOLD, 14));
        newProfileBtn = new JButton("Create New Profile");
        newProfileBtn.setFont(new Font("Arial", Font.PLAIN, 12));

        loadBtn.addActionListener(this::handleLoadProfile);
        newProfileBtn.addActionListener(e -> createNewProfile());

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.add(new JLabel("Select a user profile:"));
        center.add(profileDropdown);
        center.add(loadBtn);

        JPanel bottom = new JPanel();
        bottom.add(newProfileBtn);

        setLayout(new BorderLayout(20, 20));
        add(new JLabel("Welcome to NutriSci!"), BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void createNewProfile() {
        JFrame createFrame = new JFrame("Create New Profile");
        createFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create a callback to refresh the splash screen after profile creation
        Runnable refreshCallback = () -> {
            // Refresh the splash screen to show the new profile
            SwingUtilities.invokeLater(() -> {
                // Create a new splash screen
                SplashScreenUI newSplash = new SplashScreenUI(onProfileSelected);
                newSplash.setVisible(true);
            });
        };
        
        createFrame.add(new CreateProfilePanel(refreshCallback));
        createFrame.setSize(400, 450);
        createFrame.setLocationRelativeTo(null);
        createFrame.setVisible(true);
        
        // Close this splash screen when opening create profile window
        this.dispose();
    }

    private void handleLoadProfile(ActionEvent e) {
        String selectedProfileName = (String) profileDropdown.getSelectedItem();
        if (selectedProfileName == null) {
            JOptionPane.showOptionDialog(this, "Please select a profile.", "No Profile Selected", 
                JOptionPane.WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE, null, 
                new String[]{"OK"}, "OK");
            return;
        }

        // Find the selected profile by name
        List<UserProfile> profiles = profileManager.getProfiles();
        UserProfile selectedProfile = profiles.stream()
                .filter(p -> p.getName().equals(selectedProfileName))
                .findFirst()
                .orElse(null);

        if (selectedProfile == null) {
            JOptionPane.showMessageDialog(this, "Profile not found.", "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Set as current profile
        profileManager.setCurrentProfile(selectedProfile.getUserID());

        // Show loading message with English buttons
        JOptionPane.showOptionDialog(this, "Loading profile: " + selectedProfile.getName(), "Loading", 
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, 
            new String[]{"OK"}, "OK");
        
        // Close this window
        this.dispose();
        
        // Call the callback to continue to next step
        if (onProfileSelected != null) {
            onProfileSelected.run();
        }
    }
}

