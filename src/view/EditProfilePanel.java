package view;

import model.UserProfile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class EditProfilePanel extends JPanel {
    private JTextField nameField, dobField, heightField, weightField;
    private JComboBox<String> sexBox;
    private JRadioButton metricBtn, imperialBtn;
    private JButton saveBtn;

    public EditProfilePanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        add(nameField, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Sex:"), gbc);
        gbc.gridx = 1;
        sexBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        add(sexBox, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Date of Birth (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dobField = new JTextField(10);
        add(dobField, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Height:"), gbc);
        gbc.gridx = 1;
        heightField = new JTextField(6);
        add(heightField, gbc);

        // Row 4
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Weight:"), gbc);
        gbc.gridx = 1;
        weightField = new JTextField(6);
        add(weightField, gbc);

        // Row 5
        gbc.gridx = 0; gbc.gridy = 5;
        add(new JLabel("Units:"), gbc);
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

        // Row 6
        gbc.gridx = 1; gbc.gridy = 6;
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(this::saveProfile);
        add(saveBtn, gbc);
    }

    private void saveProfile(ActionEvent e) {
        // Placeholder logic
        JOptionPane.showMessageDialog(this, "Profile saved! (TODO: implement saving logic)");
    }

    // Optional: preload user data into form
    public void loadProfile(UserProfile profile) {
        nameField.setText(profile.getName());
        sexBox.setSelectedItem(profile.getSex());
        dobField.setText(profile.getDob().toString()); // or format
        heightField.setText(String.valueOf(profile.getHeight()));
        weightField.setText(String.valueOf(profile.getWeight()));
        if ("Metric".equalsIgnoreCase(profile.getSettings().getUnits())) {
            metricBtn.setSelected(true);
        } else {
            imperialBtn.setSelected(true);
        }
    }
}
