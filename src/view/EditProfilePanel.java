package view;

import model.user.UserProfile;
import controller.UserProfileController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EditProfilePanel extends JPanel {
    private JTextField nameField, heightField, weightField;
    private JComboBox<String> sexBox;
    private JButton dobButton;
    private JLabel dobLabel;
    private JButton saveBtn;
    private UserProfile currentProfile;
    private UserProfileController profileController;
    private LocalDate selectedDate;

    public EditProfilePanel(UserProfile profile) {
        this.currentProfile = profile;
        this.selectedDate = profile.getDob();
        
        // Initialize profile controller
        try {
            profileController = new UserProfileController();
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize database connection: " + e.getMessage());
            profileController = null;
        }
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Initialize components
        nameField = new JTextField(20);
        sexBox = new JComboBox<>(new String[]{"Male", "Female"});
        dobButton = new JButton("Select Date");
        dobLabel = new JLabel(selectedDate.toString());
        heightField = new JTextField(10);
        weightField = new JTextField(10);
        saveBtn = new JButton("Save");

        // Add components
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Sex:"), gbc);
        gbc.gridx = 1;
        add(sexBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dobLabel = new JLabel(selectedDate.toString());
        dobButton = new JButton("Select Date");
        dobButton.addActionListener(e -> showDatePicker());
        dobPanel.add(dobLabel);
        dobPanel.add(dobButton);
        add(dobPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Height (cm):"), gbc);
        gbc.gridx = 1;
        add(heightField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Weight (kg):"), gbc);
        gbc.gridx = 1;
        add(weightField, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        saveBtn = new JButton("Save");
        JButton deleteBtn = new JButton("Delete Profile");
        deleteBtn.setForeground(Color.RED);
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(deleteBtn);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // Add listeners
        saveBtn.addActionListener(this::saveProfile);
        deleteBtn.addActionListener(this::deleteProfile);

        // Load current profile data
        loadProfile(profile);

        setPreferredSize(new Dimension(400, 300));
    }

    private void showDatePicker() {
        JDialog dateDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dateDialog.setLayout(new BorderLayout());
        dateDialog.setSize(300, 200);
        dateDialog.setLocationRelativeTo(this);

        // Create year dropdown (1900-2025)
        String[] years = new String[126];
        for (int i = 0; i < 126; i++) {
            years[i] = String.valueOf(1900 + i);
        }
        JComboBox<String> yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem(String.valueOf(selectedDate.getYear()));

        // Create month dropdown
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = String.valueOf(i + 1);
        }
        JComboBox<String> monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem(String.valueOf(selectedDate.getMonthValue()));

        // Create day dropdown
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.valueOf(i + 1);
        }
        JComboBox<String> dayCombo = new JComboBox<>(days);
        dayCombo.setSelectedItem(String.valueOf(selectedDate.getDayOfMonth()));

        // Panel for dropdowns
        JPanel dropdownPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        dropdownPanel.add(new JLabel("Year:"));
        dropdownPanel.add(yearCombo);
        dropdownPanel.add(new JLabel("Month:"));
        dropdownPanel.add(monthCombo);
        dropdownPanel.add(new JLabel("Day:"));
        dropdownPanel.add(dayCombo);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            try {
                int year = Integer.parseInt((String) yearCombo.getSelectedItem());
                int month = Integer.parseInt((String) monthCombo.getSelectedItem());
                int day = Integer.parseInt((String) dayCombo.getSelectedItem());
                selectedDate = LocalDate.of(year, month, day);
                dobLabel.setText(selectedDate.toString());
                dateDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dateDialog, "Invalid date selected", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dateDialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dateDialog.add(dropdownPanel, BorderLayout.CENTER);
        dateDialog.add(buttonPanel, BorderLayout.SOUTH);
        dateDialog.setVisible(true);
    }

    private void loadProfile(UserProfile profile) {
        nameField.setText(profile.getName());
        sexBox.setSelectedItem(profile.getSex());
        dobLabel.setText(profile.getDob().toString());
        selectedDate = profile.getDob();
        heightField.setText(String.valueOf(profile.getHeight()));
        weightField.setText(String.valueOf(profile.getWeight()));
    }

    private void saveProfile(ActionEvent e) {
        try {
            String name = nameField.getText().trim();
            String sex = (String) sexBox.getSelectedItem();
            double height = Double.parseDouble(heightField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a name", "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (height <= 0 || weight <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid height and weight", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update current profile
            currentProfile.setName(name);
            currentProfile.setSex(sex);
            currentProfile.setDob(selectedDate);
            currentProfile.setHeight(height);
            currentProfile.setWeight(weight);

            // Save to database if controller is available
            boolean savedToDatabase = false;
            if (profileController != null) {
                try {
                    profileController.editProfile(name, sex, selectedDate, height, weight);
                    savedToDatabase = true;
                } catch (Exception ex) {
                    System.err.println("Error saving to database: " + ex.getMessage());
                }
            }

            String message = String.format(
                "Profile %s!\n\n" +
                "Updated Information:\n" +
                "• Name: %s\n" +
                "• Sex: %s\n" +
                "• Date of Birth: %s\n" +
                "• Height: %.1f cm\n" +
                "• Weight: %.1f kg",
                savedToDatabase ? "updated successfully in database" : "updated successfully (database not available)",
                name, sex, selectedDate.toString(), height, weight
            );

            JOptionPane.showMessageDialog(this, message, "Profile Updated", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for height and weight", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProfile(ActionEvent e) {
        // First confirmation
        int confirm = JOptionPane.showOptionDialog(this,
            "Are you sure you want to save changes to your profile?",
            "Confirm Save",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{"Yes", "No"},
            "Yes"
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Second confirmation
            int finalConfirm = JOptionPane.showOptionDialog(this,
                "Are you sure you want to delete your profile? This action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Yes", "No"},
                "Yes"
            );
            
            if (finalConfirm == JOptionPane.YES_OPTION) {
                if (profileController != null) {
                    try {
                        profileController.deleteProfile(currentProfile.getUserID());
                        JOptionPane.showMessageDialog(this, "Profile deleted successfully!", "Profile Deleted", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Close all windows and restart the application
                        Window[] windows = Window.getWindows();
                        for (Window window : windows) {
                            if (window instanceof JFrame) {
                                window.dispose();
                            }
                        }
                        
                        // Restart the application
                        SwingUtilities.invokeLater(() -> {
                            try {
                                // Restart the main application
                                app.Main.main(new String[0]);
                            } catch (Exception ex) {
                                System.err.println("Error restarting application: " + ex.getMessage());
                                System.exit(0);
                            }
                        });
                        
                    } catch (Exception ex) {
                        System.err.println("Error deleting profile: " + ex.getMessage());
                        JOptionPane.showMessageDialog(this, "Error deleting profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
        } else {
                    JOptionPane.showMessageDialog(this, "Profile deletion is not available as the database connection is not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    

}
