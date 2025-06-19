package view;

import controller.UserProfileController;
import model.user.UserProfile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class EditSettingsPanel extends JPanel {
    private final UserProfileController controller;
    private JRadioButton metricBtn, imperialBtn;
    private JButton saveBtn;

    public EditSettingsPanel(UserProfileController controller) {
        this.controller = controller;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(new JLabel("Edit Settings", SwingConstants.CENTER), gbc);

        // Units label
        gbc.gridy++; gbc.gridwidth = 1;
        add(new JLabel("Units:"), gbc);

        // Units radio buttons
        gbc.gridx = 1;
        JPanel unitPanel = new JPanel();
        metricBtn = new JRadioButton("Metric");
        imperialBtn = new JRadioButton("Imperial");
        ButtonGroup unitGroup = new ButtonGroup();
        unitGroup.add(metricBtn);
        unitGroup.add(imperialBtn);
        unitPanel.add(metricBtn);
        unitPanel.add(imperialBtn);
        add(unitPanel, gbc);
        UserProfile profile = controller.getCurrentProfile();
        if ("Metric".equalsIgnoreCase(profile.getSettings().getUnits())) {
            metricBtn.setSelected(true);
        } else {
            imperialBtn.setSelected(true);
        }
        // Save button
        gbc.gridx = 1; gbc.gridy++;
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(this::saveSettings);
        add(saveBtn, gbc);
    }

    private void saveSettings(ActionEvent e) {
        String selectedUnit = metricBtn.isSelected() ? "Metric" : "Imperial";
        controller.updateSettings(selectedUnit); // ✅ Controller handles it
        JOptionPane.showMessageDialog(this, "Settings saved!");
    }
}