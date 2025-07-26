package view;

import service.CalorieCalculator;
import model.user.UserProfile;
import controller.UserProfileController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CreateProfilePanel extends JPanel {
    private JTextField nameField, heightField, weightField;
    private JComboBox<String> sexBox;
    private JButton dobButton;
    private JButton saveBtn;
    private JLabel calorieInfoLabel, dobLabel;
    private UserProfileController profileController;
    private LocalDate selectedDate;
    private Runnable onProfileCreated; // Callback for when profile is created

    public CreateProfilePanel() {
        this(null);
    }

    public CreateProfilePanel(Runnable onProfileCreated) {
        this.onProfileCreated = onProfileCreated;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE; // Don't fill, use fixed sizes

        // Initialize components
        nameField = new JTextField(20);
        sexBox = new JComboBox<>(new String[]{"Male", "Female"});
        dobButton = new JButton("Select Date");
        dobLabel = new JLabel("1990-01-01");
        heightField = new JTextField(10);
        weightField = new JTextField(10);
        saveBtn = new JButton("Save Profile");
        calorieInfoLabel = new JLabel("Enter your information to see calorie recommendations");
        calorieInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Set text field properties to ensure proper display
        nameField.setForeground(Color.BLACK);
        nameField.setBackground(Color.WHITE);
        heightField.setForeground(Color.BLACK);
        heightField.setBackground(Color.WHITE);
        weightField.setForeground(Color.BLACK);
        weightField.setBackground(Color.WHITE);

        // Fix text field sizes to prevent layout changes
        Dimension nameSize = new Dimension(200, 25);
        Dimension numberSize = new Dimension(100, 25);
        nameField.setPreferredSize(nameSize);
        nameField.setMinimumSize(nameSize);
        nameField.setMaximumSize(nameSize);
        heightField.setPreferredSize(numberSize);
        heightField.setMinimumSize(numberSize);
        heightField.setMaximumSize(numberSize);
        weightField.setPreferredSize(numberSize);
        weightField.setMinimumSize(numberSize);
        weightField.setMaximumSize(numberSize);

        // Set default date
        selectedDate = LocalDate.of(1990, 1, 1);

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
        dobLabel = new JLabel("1990-01-01");
        dobButton = new JButton("Select Date");
        dobButton.addActionListener(e -> showDatePicker());
        dobPanel.add(dobLabel); // Display label first
        dobPanel.add(dobButton); // Then button
        add(dobPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Height (cm):"), gbc);
        gbc.gridx = 1;
        add(heightField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Weight (kg):"), gbc);
        gbc.gridx = 1;
        add(weightField, gbc);

        // Add calorie info label
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(calorieInfoLabel, gbc);

        // Add buttons panel
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton backBtn = new JButton("Back to User Selection");
        backBtn.addActionListener(e -> goBackToUserSelection());
        buttonPanel.add(backBtn);
        buttonPanel.add(saveBtn);
        add(buttonPanel, gbc);

        // Add listeners
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { updateCalorieInfo(); }
            public void removeUpdate(DocumentEvent e) { updateCalorieInfo(); }
            public void insertUpdate(DocumentEvent e) { updateCalorieInfo(); }
        });

        sexBox.addActionListener(e -> updateCalorieInfo());
        heightField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { updateCalorieInfo(); }
            public void removeUpdate(DocumentEvent e) { updateCalorieInfo(); }
            public void insertUpdate(DocumentEvent e) { updateCalorieInfo(); }
        });

        weightField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { updateCalorieInfo(); }
            public void removeUpdate(DocumentEvent e) { updateCalorieInfo(); }
            public void insertUpdate(DocumentEvent e) { updateCalorieInfo(); }
        });

        saveBtn.addActionListener(this::saveProfile);

        // Initialize profile controller
        try {
            profileController = new UserProfileController();
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize database connection: " + e.getMessage());
            profileController = null;
        }

        setPreferredSize(new Dimension(400, 350));
    }

    private void showDatePicker() {
        // Create a simple date picker dialog
        JDialog dateDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dateDialog.setLayout(new BorderLayout());
        dateDialog.setSize(300, 200);
        dateDialog.setLocationRelativeTo(this);

        // Year dropdown (1900-2024)
        String[] years = new String[125];
        for (int i = 0; i < 125; i++) {
            years[i] = String.valueOf(1900 + i);
        }
        JComboBox<String> yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem(String.valueOf(selectedDate.getYear()));

        // Month dropdown (1-12)
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = String.valueOf(i + 1);
        }
        JComboBox<String> monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem(String.valueOf(selectedDate.getMonthValue()));

        // Day dropdown (1-31)
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
                LocalDate newDate = LocalDate.of(year, month, day);
                
                // Check if the selected date is in the future
                LocalDate today = LocalDate.now();
                if (newDate.isAfter(today)) {
                    JOptionPane.showMessageDialog(dateDialog, 
                        "Birth date cannot be in the future. Please select a valid birth date.", 
                        "Invalid Date", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                selectedDate = newDate;
                dobLabel.setText(selectedDate.toString());
                dateDialog.dispose();
                updateCalorieInfo();
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

    private void updateCalorieInfo() {
        try {
            String name = nameField.getText().trim();
            String sex = (String) sexBox.getSelectedItem();
            String heightText = heightField.getText().trim();
            String weightText = weightField.getText().trim();

            // Debug: print current values
            System.out.println("Debug - Name: '" + name + "', Height: '" + heightText + "', Weight: '" + weightText + "'");

            if (name.isEmpty() || heightText.isEmpty() || weightText.isEmpty()) {
                calorieInfoLabel.setText("Please enter valid information to see calorie recommendations");
                return;
            }

            double height = Double.parseDouble(heightText);
            double weight = Double.parseDouble(weightText);

            if (height <= 0 || weight <= 0) {
                calorieInfoLabel.setText("Please enter valid positive numbers for height and weight");
                return;
            }

            // Create temporary profile for calculation
            UserProfile tempProfile = new UserProfile(name, sex, selectedDate, height, weight);
            
            // Calculate using Mifflin-St Jeor method (International Standard)
            double bmr = CalorieCalculator.calculateBMR(tempProfile);
            double tdee = CalorieCalculator.calculateTDEE(tempProfile, "Lightly active (light exercise 1-3 days/week)");
            double recommendedCalories = CalorieCalculator.getRecommendedCaloriesMifflin(tempProfile, 
                "Lightly active (light exercise 1-3 days/week)");

            String info = String.format(
                "<html><div style='text-align: center;'>" +
                "<b>Daily Calorie Recommendations (Mifflin-St Jeor - International Standard):</b><br>" +
                "• BMR: %.0f cal/day<br>" +
                "• TDEE: %.0f cal/day<br>" +
                "• Recommended: %.0f cal/day<br><br>" +
                "<i>Note: Activity level and goals can be set in the main menu.</i></div></html>",
                bmr, tdee, recommendedCalories
            );
            calorieInfoLabel.setText(info);

        } catch (NumberFormatException e) {
            calorieInfoLabel.setText("Please enter valid numbers for height and weight");
            System.err.println("NumberFormatException: " + e.getMessage());
        } catch (Exception e) {
            calorieInfoLabel.setText("Error calculating calories: " + e.getMessage());
            System.err.println("Exception in updateCalorieInfo: " + e.getMessage());
        }
    }

    private void goBackToUserSelection() {
        // Close the create profile window
        SwingUtilities.getWindowAncestor(this).dispose();
        
        // Call the callback to return to splash screen
        if (onProfileCreated != null) {
            onProfileCreated.run();
        }
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

            // Create profile for calculation
            UserProfile newProfile = new UserProfile(name, sex, selectedDate, height, weight);
            
            // Try to save to database if available
            boolean savedToDatabase = false;
            if (profileController != null) {
                try {
                    profileController.createProfile(name, sex, selectedDate, height, weight);
                    savedToDatabase = true;
                } catch (Exception dbEx) {
                    System.err.println("Database save failed: " + dbEx.getMessage());
                }
            }
            
            // Calculate and show calorie information
            double bmr = CalorieCalculator.calculateBMR(newProfile);
            double tdee = CalorieCalculator.calculateTDEE(newProfile, "Lightly active (light exercise 1-3 days/week)");
            double recommendedCalories = CalorieCalculator.getRecommendedCaloriesMifflin(newProfile, 
                "Lightly active (light exercise 1-3 days/week)");

            String message = String.format(
                "Profile %s!\n\n" +
                "Calorie Information (Mifflin-St Jeor - International Standard):\n" +
                "• BMR: %.0f cal/day\n" +
                "• TDEE: %.0f cal/day\n" +
                "• Recommended: %.0f cal/day\n\n" +
                "You can set activity level and goals in the main menu.",
                savedToDatabase ? "saved successfully to database" : "created successfully (database not available)",
                bmr, tdee, recommendedCalories
            );

            JOptionPane.showMessageDialog(this, message, "Profile Created", 
                JOptionPane.INFORMATION_MESSAGE);

            // Close the create profile window
            SwingUtilities.getWindowAncestor(this).dispose();

            // Call the callback if it's not null (for returning to splash screen)
            if (onProfileCreated != null) {
                onProfileCreated.run();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for height and weight", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error creating profile: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}