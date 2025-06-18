package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;

class MealEntryPanel extends JPanel {
    private JComboBox<String> mealTypeBox;
    private JSpinner dateSpinner;
    private JTable ingredientTable;
    private DefaultTableModel tableModel;

    private JButton addIngredientBtn, removeIngredientBtn, submitMealBtn;

    public MealEntryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder("Log a New Meal"));

        // ===== Top Panel: Date + Meal Type =====
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        topPanel.add(new JLabel("Date:"));
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        topPanel.add(dateSpinner);

        topPanel.add(new JLabel("Meal Type:"));
        mealTypeBox = new JComboBox<>(new String[]{"Breakfast", "Lunch", "Dinner", "Snack"});
        topPanel.add(mealTypeBox);
        add(topPanel, BorderLayout.NORTH);

        // ===== Center Panel: Ingredient Table =====
        tableModel = new DefaultTableModel(new Object[]{"Ingredient", "Quantity (g)"}, 0);
        ingredientTable = new JTable(tableModel);
        ingredientTable.setRowHeight(24);
        ingredientTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(ingredientTable);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        add(scrollPane, BorderLayout.CENTER);

        // ===== Bottom Panel: Buttons =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addIngredientBtn = new JButton("Add Ingredient");
        removeIngredientBtn = new JButton("Remove Selected");
        submitMealBtn = new JButton("Submit Meal");

        addIngredientBtn.addActionListener(e -> showAddIngredientDialog());
        removeIngredientBtn.addActionListener(e -> {
            int selected = ingredientTable.getSelectedRow();
            if (selected != -1) tableModel.removeRow(selected);
        });
        submitMealBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Meal submitted (mock)!"));

        bottomPanel.add(addIngredientBtn);
        bottomPanel.add(removeIngredientBtn);
        bottomPanel.add(submitMealBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void showAddIngredientDialog() {
        String[] mockIngredients = {"Tomatoes", "Bread", "Eggs", "Beef", "Cheese", "Lettuce", "Rice", "Chicken"};
        JComboBox<String> ingredientBox = new JComboBox<>(mockIngredients);
        JTextField quantityField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Ingredient:"));
        panel.add(ingredientBox);
        panel.add(new JLabel("Quantity (g):"));
        panel.add(quantityField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Ingredient", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String ingredient = (String) ingredientBox.getSelectedItem();
            String quantity = quantityField.getText();
            if (!ingredient.isEmpty() && !quantity.isEmpty()) {
                tableModel.addRow(new Object[]{ingredient, quantity});
            }
        }
    }

    // OPTIONAL: for integration
    public Date getSelectedDate() {
        return (Date) dateSpinner.getValue();
    }

    public String getMealType() {
        return (String) mealTypeBox.getSelectedItem();
    }

    public DefaultTableModel getIngredientData() {
        return tableModel;
    }
}
