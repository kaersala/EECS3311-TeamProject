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
    private JList<String> nutrientList;
    private JComboBox<String> directionBox;
    private JComboBox<String> intensityBox;
    private JButton confirmBtn;
    private DefaultListModel<String> nutrientModel;
    private List<String> availableNutrients;
    private List<Goal> selectedGoals;
    private JLabel targetAmountLabel;
    private JLabel unitLabel;
    private DefaultListModel<String> currentGoalsModel;

    public GoalSelectionUI() {
        this(new ArrayList<>());
    }
    
    public GoalSelectionUI(List<Goal> existingGoals) {
        this.selectedGoals = new ArrayList<>(existingGoals);
        this.availableNutrients = List.of("Fiber", "Calories", "Protein", "Fat", "Carbohydrates", "Sodium");
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Select Your Nutrition Goals", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Main content panel - use horizontal split layout
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 15, 10));

        // Left panel: current goals
        JPanel currentGoalsPanel = new JPanel(new BorderLayout(5, 5));
        currentGoalsPanel.setBorder(BorderFactory.createTitledBorder("Current Goals"));
        
        // Create current goals list
        currentGoalsModel = new DefaultListModel<>();
        for (Goal goal : selectedGoals) {
            currentGoalsModel.addElement(goal.getNutrient() + ": " + goal.getDirection() + 
                " by " + goal.getAmount() + " (" + goal.getIntensity() + ")");
        }
        
        JList<String> currentGoalsList = new JList<>(currentGoalsModel);
        currentGoalsList.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JScrollPane currentGoalsScrollPane = new JScrollPane(currentGoalsList);
        currentGoalsPanel.add(currentGoalsScrollPane, BorderLayout.CENTER);
        
        // Remove goal button
        JButton removeGoalBtn = new JButton("Remove Selected Goal");
        removeGoalBtn.addActionListener(e -> {
            int selectedIndex = currentGoalsList.getSelectedIndex();
            System.out.println("DEBUG: Remove goal clicked, selectedIndex = " + selectedIndex);
            if (selectedIndex >= 0) {
                Goal removedGoal = selectedGoals.remove(selectedIndex);
                currentGoalsModel.remove(selectedIndex);
                System.out.println("DEBUG: Removed goal: " + (removedGoal != null ? removedGoal.toString() : "null"));
                System.out.println("DEBUG: Remaining goals count: " + selectedGoals.size());
            } else {
                System.out.println("DEBUG: No goal selected for removal");
            }
        });
        currentGoalsPanel.add(removeGoalBtn, BorderLayout.SOUTH);
        
        contentPanel.add(currentGoalsPanel);
        
        // Right panel: add new goal
        JPanel addGoalPanel = new JPanel(new BorderLayout(10, 10));
        addGoalPanel.setBorder(BorderFactory.createTitledBorder("Add New Goal"));
        
        // Step 1: Select Nutrient
        JPanel nutrientPanel = new JPanel(new BorderLayout(5, 5));
        nutrientPanel.setBorder(BorderFactory.createTitledBorder("Step 1: Select Nutrient"));
        
        nutrientModel = new DefaultListModel<>();
        for (String nutrient : availableNutrients) {
            nutrientModel.addElement(nutrient);
        }

        nutrientList = new JList<>(nutrientModel);
        nutrientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nutrientList.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JScrollPane nutrientScrollPane = new JScrollPane(nutrientList);
        nutrientPanel.add(nutrientScrollPane, BorderLayout.CENTER);
        addGoalPanel.add(nutrientPanel, BorderLayout.CENTER);

        // Step 2: Goal Settings Panel
        JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Step 2: Goal Settings"));

        // Direction Selection
        settingsPanel.add(new JLabel("Direction:"));
        directionBox = new JComboBox<>(new String[]{"Increase", "Decrease"});
        directionBox.setSelectedIndex(0); // Default to Increase
        settingsPanel.add(directionBox);

        // Intensity Selection
        settingsPanel.add(new JLabel("Intensity:"));
        intensityBox = new JComboBox<>(new String[]{"Low", "Moderate", "High"});
        intensityBox.setSelectedIndex(1); // Default to Moderate
        settingsPanel.add(intensityBox);

        // Target Amount (Auto-calculated)
        settingsPanel.add(new JLabel("Target Amount:"));
        targetAmountLabel = new JLabel("--");
        targetAmountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        settingsPanel.add(targetAmountLabel);

        // Unit
        settingsPanel.add(new JLabel("Unit:"));
        unitLabel = new JLabel("--");
        unitLabel.setFont(new Font("Arial", Font.BOLD, 12));
        settingsPanel.add(unitLabel);

        addGoalPanel.add(settingsPanel, BorderLayout.SOUTH);
        contentPanel.add(addGoalPanel);
        add(contentPanel, BorderLayout.CENTER);

        // Confirm Button
        confirmBtn = new JButton("Add Goal");
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 14));
        confirmBtn.addActionListener(this::handleAddGoal);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(confirmBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners to update target amount when selections change
        nutrientList.addListSelectionListener(e -> updateTargetAmount());
        directionBox.addActionListener(e -> updateTargetAmount());
        intensityBox.addActionListener(e -> updateTargetAmount());
    }

    private void updateTargetAmount() {
        String selectedNutrient = nutrientList.getSelectedValue();
        System.out.println("DEBUG: updateTargetAmount called, selectedNutrient = '" + selectedNutrient + "'");
        
        if (selectedNutrient != null) {
            String direction = (String) directionBox.getSelectedItem();
            String intensity = (String) intensityBox.getSelectedItem();
            
            System.out.println("DEBUG: direction = '" + direction + "', intensity = '" + intensity + "'");
            
            // Calculate target amount based on intensity
            double targetAmount = calculateTargetAmount(selectedNutrient, intensity);
            String unit = getUnitForNutrient(selectedNutrient);
            
            System.out.println("DEBUG: calculated targetAmount = " + targetAmount + ", unit = '" + unit + "'");
            
            targetAmountLabel.setText(String.format("%.1f", targetAmount));
            unitLabel.setText(unit);
            
            // Force UI update
            targetAmountLabel.revalidate();
            targetAmountLabel.repaint();
            unitLabel.revalidate();
            unitLabel.repaint();
        } else {
            System.out.println("DEBUG: selectedNutrient is null, setting --");
            targetAmountLabel.setText("--");
            unitLabel.setText("--");
        }
    }

    private double calculateTargetAmount(String nutrient, String intensity) {
        // Define target amounts based on nutrient and intensity
        double baseAmount = switch (nutrient.toLowerCase()) {
            case "fiber" -> 5.0;      // 5g base
            case "calories" -> 200.0;  // 200 calories base
            case "protein" -> 10.0;    // 10g base
            case "fat" -> 15.0;        // 15g base
            case "carbohydrates" -> 25.0; // 25g base
            case "sodium" -> 500.0;    // 500mg base
            default -> 5.0;
        };
        
        // Adjust based on intensity
        return switch (intensity.toLowerCase()) {
            case "low" -> baseAmount * 0.5;
            case "moderate" -> baseAmount;
            case "high" -> baseAmount * 1.5;
            default -> baseAmount;
        };
    }

    private String getUnitForNutrient(String nutrient) {
        return switch (nutrient.toLowerCase()) {
            case "calories" -> "calories";
            case "protein", "fiber", "fat", "carbohydrates" -> "grams";
            case "sodium" -> "mg";
            default -> "units";
        };
    }

    private void handleAddGoal(ActionEvent e) {
        String selectedNutrient = nutrientList.getSelectedValue();
        
        if (selectedNutrient == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a nutrient first.", 
                "No Nutrient Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if we already have 2 goals
        if (selectedGoals.size() >= 2) {
            JOptionPane.showMessageDialog(this, 
                "You can only set 1 or 2 goals maximum.", 
                "Goal Limit Reached", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if this nutrient is already selected
        for (Goal goal : selectedGoals) {
            if (goal.getNutrient().equals(selectedNutrient)) {
                JOptionPane.showMessageDialog(this, 
                    "You have already selected " + selectedNutrient + ". Please choose a different nutrient.", 
                    "Duplicate Goal", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String direction = (String) directionBox.getSelectedItem();
        String intensity = (String) intensityBox.getSelectedItem();
        double targetAmount = calculateTargetAmount(selectedNutrient, intensity);

        Goal newGoal = new Goal(selectedNutrient, direction, targetAmount, intensity.toLowerCase());
        selectedGoals.add(newGoal);
        
        // Update the left panel list
        currentGoalsModel.addElement(newGoal.getNutrient() + ": " + newGoal.getDirection() + 
            " by " + newGoal.getAmount() + " (" + newGoal.getIntensity() + ")");
        
        System.out.println("DEBUG: Added goal: " + newGoal.toString());
        System.out.println("DEBUG: Total goals now: " + selectedGoals.size());

        // Clear selection for next goal
        nutrientList.clearSelection();
        updateTargetAmount();

        JOptionPane.showMessageDialog(this, 
            "Goal added: " + direction + " " + selectedNutrient + " by " + 
            String.format("%.1f", targetAmount) + " " + getUnitForNutrient(selectedNutrient) + 
            " (" + intensity + " intensity)\n\n" +
            "Goals set: " + selectedGoals.size() + "/2", 
            "Goal Added", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    public List<Goal> getSelectedGoals() {
        return selectedGoals;
    }

    public void clearSelectedGoals() {
        selectedGoals.clear();
    }
}
