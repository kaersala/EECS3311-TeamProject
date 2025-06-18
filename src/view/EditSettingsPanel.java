package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class EditSettingsPanel extends JPanel {
    private JRadioButton metricBtn, imperialBtn;
    private JButton saveBtn;

    public EditSettingsPanel() {
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

        // Save button
        gbc.gridx = 1; gbc.gridy++;
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(this::saveSettings);
        add(saveBtn, gbc);
    }

    private void saveSettings(ActionEvent e) {
        String selectedUnit = metricBtn.isSelected() ? "Metric" : "Imperial";
        // TODO: Apply this to the current UserProfile's Settings
        JOptionPane.showMessageDialog(this, "Settings saved as " + selectedUnit);
    }
}
