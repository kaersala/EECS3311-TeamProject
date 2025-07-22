package view;

import dao.adapter.DatabaseAdapter;
import dao.adapter.MySQLAdapter;
import model.FoodItem;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class MealEntryPanel extends JPanel {
    private JComboBox<String> mealTypeBox;
    private JSpinner dateSpinner;
    private JTable ingredientTable;
    private DefaultTableModel tableModel;
    private DatabaseAdapter databaseAdapter;
    private List<FoodItem> foodItems;

    private JButton addIngredientBtn, removeIngredientBtn, submitMealBtn;

    public MealEntryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder("Log a New Meal"));
        
        // Initialize database connection and load food items
        initializeDatabase();

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
    
    private void initializeDatabase() {
        try {
            databaseAdapter = new MySQLAdapter();
            databaseAdapter.connect();
            foodItems = databaseAdapter.loadFoods();
            System.out.println("Loaded " + foodItems.size() + " food items from database");
        } catch (Exception e) {
            System.err.println("Failed to load food items from database: " + e.getMessage());
            // Fallback to empty list
            foodItems = new java.util.ArrayList<>();
        }
    }

    private void showAddIngredientDialog() {
        // Create food names array from database
        String[] foodNames;
        if (foodItems != null && !foodItems.isEmpty()) {
            foodNames = foodItems.stream()
                    .map(FoodItem::getName)
                    .toArray(String[]::new);
        } else {
            // Fallback to mock data if database loading failed
            foodNames = new String[]{"Tomatoes", "Bread", "Eggs", "Beef", "Cheese", "Lettuce", "Rice", "Chicken"};
        }
        
        JComboBox<String> ingredientBox = new JComboBox<>(foodNames);
        JTextField quantityField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Ingredient:"));
        panel.add(ingredientBox);
        panel.add(new JLabel("Quantity (g) - Food weight:"));
        panel.add(quantityField);

        int result = JOptionPane.showOptionDialog(this, panel, "Add Ingredient", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Confirm", "Cancel"}, "Confirm");
        if (result == 0) { // 0 = Confirm button, 1 = Cancel button
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
