package view;

import controller.UserProfileController;

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
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel title = new JLabel("Edit Settings", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, gbc);

        // Units Label
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(new JLabel("Units:"), gbc);

        // Radio Buttons
        gbc.gridx = 1;
        JPanel unitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        metricBtn = new JRadioButton("Metric");
        imperialBtn = new JRadioButton("Imperial");
        ButtonGroup unitGroup = new ButtonGroup();
        unitGroup.add(metricBtn);
        unitGroup.add(imperialBtn);
        unitPanel.add(metricBtn);
        unitPanel.add(imperialBtn);
        add(unitPanel, gbc);

        // Pre-select current setting
        String currentSetting = controller.getUserSettings();
        if (currentSetting != null && currentSetting.equalsIgnoreCase("Imperial")) {
            imperialBtn.setSelected(true);
        } else {
            metricBtn.setSelected(true);
        }

        // Save Button
        gbc.gridy++;
        gbc.gridx = 1;
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(this::saveSettings);
        add(saveBtn, gbc);
    }

    private void saveSettings(ActionEvent e) {
        String selectedUnit = metricBtn.isSelected() ? "Metric" : "Imperial";
        controller.updateSettings(selectedUnit);
        JOptionPane.showMessageDialog(this, "Settings updated to " + selectedUnit + "!");
    }
}
