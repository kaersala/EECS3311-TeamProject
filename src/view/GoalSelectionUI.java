package view;

import model.Goal;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GoalSelectionUI extends JPanel {
    private JList<String> goalList;
    private JComboBox<String> intensityBox;
    private JButton confirmBtn;
    private DefaultListModel<String> goalModel;
    private List<Goal> predefinedGoals;
    private List<Goal> selectedGoals;

    public GoalSelectionUI(List<Goal> goals) {
        this.predefinedGoals = goals;
        this.selectedGoals = new ArrayList<>();
        setLayout(new BorderLayout());

        // Display Goals
        goalModel = new DefaultListModel<>();
        for (Goal g : goals) {
            goalModel.addElement(g.getNutrient() + " (" + g.getDirection() + ")");
        }

        goalList = new JList<>(goalModel);
        goalList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Add listener to restrict selection to max 2
        goalList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (goalList.getSelectedIndices().length > 2) {
                    // Deselect the most recent selection
                    int[] selected = goalList.getSelectedIndices();
                    int last = selected[selected.length - 1];
                    goalList.removeSelectionInterval(last, last);

                    JOptionPane.showMessageDialog(
                        GoalSelectionUI.this,
                        "You can select only 1 or 2 goals.",
                        "Limit Exceeded",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(goalList);
        add(scrollPane, BorderLayout.CENTER);

        // Intensity Selection
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        intensityBox = new JComboBox<>(new String[]{"Low", "Moderate", "High"});
        bottomPanel.add(new JLabel("Select Intensity:"));
        bottomPanel.add(intensityBox);

        // Confirm Button
        confirmBtn = new JButton("Confirm Goals");
        confirmBtn.addActionListener(this::handleConfirm);
        bottomPanel.add(confirmBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void handleConfirm(ActionEvent e) {
        selectedGoals.clear();
        List<String> selectedTexts = goalList.getSelectedValuesList();

        if (selectedTexts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least 1 goal.", "No Goals Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedIntensity = (String) intensityBox.getSelectedItem();

        for (String label : selectedTexts) {
            for (Goal g : predefinedGoals) {
                String goalLabel = g.getNutrient() + " (" + g.getDirection() + ")";
                if (goalLabel.equals(label)) {
                    Goal adjusted = new Goal(g.getNutrient(), g.getDirection(), g.getAmount(), selectedIntensity.toLowerCase());
                    selectedGoals.add(adjusted);
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Goals confirmed.");
    }

    public List<Goal> getSelectedGoals() {
        return selectedGoals;
    }
}
