package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SplashScreenUI extends JFrame {
    private JComboBox<String> profileDropdown;
    private JButton loadBtn, editProfileBtn, settingsBtn, newProfileBtn;

    public SplashScreenUI(List<String> profiles) {
        setTitle("NutriSci - Select Profile");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel welcomeLabel = new JLabel("Welcome to NutriSci!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        profileDropdown = new JComboBox<>(profiles.toArray(new String[0]));

        loadBtn = new JButton("Load Profile");
        editProfileBtn = new JButton("Edit Profile");
        settingsBtn = new JButton("Edit Settings");
        newProfileBtn = new JButton("Create New Profile");

        // Action Listeners (to be implemented)
        loadBtn.addActionListener(this::handleLoadProfile);
        //editProfileBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Edit Profile not yet implemented."));
        settingsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings screen not yet implemented."));
        newProfileBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Create Profile not yet implemented."));

        JPanel center = new JPanel(new GridLayout(5, 1, 10, 10));
        center.add(new JLabel("Select a user profile:"));
        center.add(profileDropdown);
        center.add(loadBtn);
        center.add(editProfileBtn);
        center.add(settingsBtn);

        JPanel bottom = new JPanel();
        bottom.add(newProfileBtn);

        editProfileBtn.addActionListener(e -> {
            JFrame editFrame = new JFrame("Edit Profile");
            EditProfilePanel editPanel = new EditProfilePanel();

//            // OPTIONAL: Load current user data from UserProfileManager
//            UserProfile current = UserProfileManager.getInstance().getCurrentProfile();
//            if (current != null) {
//                editPanel.loadProfile(current);
//            }

            editFrame.add(editPanel);
            editFrame.setSize(400, 400);
            editFrame.setLocationRelativeTo(null); // Center on screen
            editFrame.setVisible(true);
        });
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

        // Simulate loading
        JOptionPane.showMessageDialog(this, "Loading profile: " + selectedProfile);
        // TODO: Call UserProfileManager.loadProfile(id), then launch main UI
    }
}
