package view;

import dao.adapter.DatabaseAdapter;
import dao.adapter.MySQLAdapter;
import model.FoodItem;
import model.meal.MealType;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class MealEntryPanel extends JPanel {
    private JComboBox<String> mealTypeBox;
    private JLabel dateLabel;
    private JButton dateButton;
    private LocalDate selectedDate;
    private JTable ingredientTable;
    private DefaultTableModel tableModel;
    private DatabaseAdapter databaseAdapter;
    private List<FoodItem> foodItems;
    private int currentUserId;

    private JButton addIngredientBtn, removeIngredientBtn, submitMealBtn;
    
    // Calorie statistics labels
    private JLabel totalCaloriesLabel;
    private JLabel remainingLabel;
    private static final double TARGET_CALORIES = 2000.0;
    
    // Calculate food calories
    private double calculateFoodCalories(int foodId, double quantity) {
        if (foodItems != null) {
            for (FoodItem food : foodItems) {
                if (food.getFoodID() == foodId) {
                    // Use the same calorie calculation method as JournalPanel
                    double caloriesPer100g = food.getCalories();
                    return (caloriesPer100g * quantity) / 100.0;
                }
            }
        }
        return 0.0;
    }
    
    // Update total calories display
    private void updateCaloriesDisplay() {
        double totalCalories = 0.0;
        
        // Calculate total calories for all foods in the table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String foodName = (String) tableModel.getValueAt(i, 0);
            Double quantity = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
            
            // Find food ID
            int foodId = findFoodIdByName(foodName);
            if (foodId != -1) {
                double calories = calculateFoodCalories(foodId, quantity);
                totalCalories += calories;
                
                // Update calories column in the table
                tableModel.setValueAt(String.format("%.0f", calories), i, 3);
            }
        }
        
        // Update total calories display
        totalCaloriesLabel.setText(String.format("Total Calories: %.0f", totalCalories));
        
        double remaining = TARGET_CALORIES - totalCalories;
        if (remaining >= 0) {
            remainingLabel.setText(String.format("Remaining: %.0f kcal", remaining));
            remainingLabel.setForeground(new Color(0, 128, 0)); // Green
        } else {
            remainingLabel.setText(String.format("Over: %.0f kcal", Math.abs(remaining)));
            remainingLabel.setForeground(new Color(255, 0, 0)); // Red
        }
    }

    public MealEntryPanel() {
        this(1); // Default user ID
    }
    
    public MealEntryPanel(int userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder("Log a New Meal"));
        
        // Initialize database connection and load food items
        initializeDatabase();

        // Initialize selected date to today
        selectedDate = LocalDate.now();
        
        // ===== Top Panel: Date Only =====
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.add(new JLabel("Date:"));
        
        // Date selection panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dateLabel = new JLabel(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateLabel.setBorder(BorderFactory.createEtchedBorder());
        dateLabel.setPreferredSize(new Dimension(120, 25));
        dateButton = new JButton("Select Date");
        dateButton.addActionListener(e -> showDatePicker());
        datePanel.add(dateLabel);
        datePanel.add(dateButton);
        topPanel.add(datePanel);
        add(topPanel, BorderLayout.NORTH);

        // ===== Center Panel: Food Table =====
        tableModel = new DefaultTableModel(new Object[]{"Food", "Quantity (g)", "Meal Type", "Calories", "Meal ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only Quantity column is editable
            }
        };
        ingredientTable = new JTable(tableModel);
        ingredientTable.setRowHeight(24);
        ingredientTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Hide the Meal ID column
        ingredientTable.getColumnModel().getColumn(4).setMinWidth(0);
        ingredientTable.getColumnModel().getColumn(4).setMaxWidth(0);
        ingredientTable.getColumnModel().getColumn(4).setWidth(0);

        // Add table model listener to update calories when quantity changes
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 1) { // Quantity column changed
                int row = e.getFirstRow();
                Object mealIdObj = tableModel.getValueAt(row, 4); // Meal ID column
                
                if (mealIdObj != null && !mealIdObj.toString().equals("")) {
                    try {
                        int mealId = Integer.parseInt(mealIdObj.toString());
                        String newQuantityStr = tableModel.getValueAt(row, 1).toString();
                        double newQuantity = Double.parseDouble(newQuantityStr);
                        
                        // Update the quantity in the database
                        if (databaseAdapter != null && databaseAdapter.getConnection() != null) {
                            // Get the food ID for this row
                            String foodName = (String) tableModel.getValueAt(row, 0);
                            int foodId = findFoodIdByName(foodName);
                            
                            if (foodId != -1) {
                                // Update the ingredient quantity in the database
                                databaseAdapter.updateIngredientQuantity(mealId, foodId, newQuantity);
                                System.out.println("Updated quantity for meal " + mealId + ", food " + foodId + " to " + newQuantity);
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Error updating quantity in database: " + ex.getMessage());
                    }
                }
                
                // Update calories display
                updateCaloriesDisplay();
            }
        });

        JScrollPane scrollPane = new JScrollPane(ingredientTable);
        scrollPane.setPreferredSize(new Dimension(500, 200));

        // ===== Summary Panel =====
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalCaloriesLabel = new JLabel("Total Calories: 0");
        JLabel targetCaloriesLabel = new JLabel("Target: 2000 kcal");
        remainingLabel = new JLabel("Remaining: 2000 kcal");
        
        // Style the labels
        totalCaloriesLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        targetCaloriesLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        remainingLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        summaryPanel.add(totalCaloriesLabel);
        summaryPanel.add(new JLabel(" | "));
        summaryPanel.add(targetCaloriesLabel);
        summaryPanel.add(new JLabel(" | "));
        summaryPanel.add(remainingLabel);
        
        // Add summary panel to center
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);

        // ===== Bottom Panel: Buttons =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addIngredientBtn = new JButton("Add Food");
        removeIngredientBtn = new JButton("Remove Selected");

        addIngredientBtn.addActionListener(e -> showAddIngredientDialog());
        removeIngredientBtn.addActionListener(e -> {
            int selected = ingredientTable.getSelectedRow();
            if (selected != -1) {
                // Get the meal ID before removing
                Object mealIdObj = tableModel.getValueAt(selected, 4);
                if (mealIdObj != null && !mealIdObj.toString().equals("")) {
                    // This is an existing meal, delete it from database
                    try {
                        int mealId = Integer.parseInt(mealIdObj.toString());
                        if (databaseAdapter != null && databaseAdapter.getConnection() != null) {
                            databaseAdapter.deleteMeal(mealId);
                            System.out.println("Deleted meal ID: " + mealId);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error deleting meal: " + ex.getMessage());
                    }
                }
                tableModel.removeRow(selected);
                updateCaloriesDisplay(); // Update calories after removing a row
            }
        });

        // Add refresh button to reload meals
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadTodaysMeals());
        
        bottomPanel.add(addIngredientBtn);
        bottomPanel.add(removeIngredientBtn);
        bottomPanel.add(refreshBtn);

        add(bottomPanel, BorderLayout.SOUTH);
        
        // Load existing meals for today
        loadTodaysMeals();
    }
    
    private void initializeDatabase() {
        try {
            databaseAdapter = new MySQLAdapter();
            databaseAdapter.connect();
            foodItems = databaseAdapter.loadFoods();
            System.out.println("Loaded " + foodItems.size() + " food items from database");
            
            // Debug: Show first few food items
            if (foodItems.size() > 0) {
                System.out.println("First 5 food items:");
                for (int i = 0; i < Math.min(5, foodItems.size()); i++) {
                    System.out.println("  " + foodItems.get(i).getName());
                }
                
                // Debug: Check for chips-related foods
                System.out.println("Checking for chips-related foods:");
                int chipsCount = 0;
                for (FoodItem food : foodItems) {
                    if (food.getName().toLowerCase().contains("chip")) {
                        System.out.println("  Found: " + food.getName());
                        chipsCount++;
                        if (chipsCount >= 10) break; // Show first 10
                    }
                }
                if (chipsCount == 0) {
                    System.out.println("  No foods containing 'chip' found in database");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load food items from database: " + e.getMessage());
            e.printStackTrace();
            // Fallback to CSV data
            try {
                foodItems = dao.adapter.CSVAdapter.loadFoodItemsFromCSV("src/csv/FOOD NAME.csv");
                System.out.println("Loaded " + foodItems.size() + " food items from CSV");
            } catch (Exception csvException) {
                System.err.println("Failed to load food items from CSV: " + csvException.getMessage());
                // Final fallback to mock data
                foodItems = createMockFoodItems();
                System.out.println("Using mock food items");
            }
        }
    }
    
    private List<FoodItem> createMockFoodItems() {
        List<FoodItem> mockItems = new ArrayList<>();
        
        // Add some common foods including chips
        Map<String, Double> chipsNutrients = new HashMap<>();
        chipsNutrients.put("Protein", 6.0);
        chipsNutrients.put("Fat", 35.0);
        chipsNutrients.put("Carbs", 53.0);
        chipsNutrients.put("Fiber", 4.0);
        mockItems.add(new FoodItem(5989, "Potato chips, plain, salted", 536.0, chipsNutrients, "Snacks"));
        
        Map<String, Double> cookieNutrients = new HashMap<>();
        cookieNutrients.put("Protein", 4.0);
        cookieNutrients.put("Fat", 24.0);
        cookieNutrients.put("Carbs", 67.0);
        cookieNutrients.put("Fiber", 2.0);
        mockItems.add(new FoodItem(5987, "Chocolate chip cookies, commercial", 502.0, cookieNutrients, "Snacks"));
        
        Map<String, Double> granolaNutrients = new HashMap<>();
        granolaNutrients.put("Protein", 8.0);
        granolaNutrients.put("Fat", 12.0);
        granolaNutrients.put("Carbs", 72.0);
        granolaNutrients.put("Fiber", 6.0);
        mockItems.add(new FoodItem(5989, "Granola bar, chewy, chocolate chip", 420.0, granolaNutrients, "Snacks"));
        
        Map<String, Double> appleNutrients = new HashMap<>();
        appleNutrients.put("Protein", 0.3);
        appleNutrients.put("Fat", 0.2);
        appleNutrients.put("Carbs", 14.0);
        appleNutrients.put("Fiber", 2.4);
        mockItems.add(new FoodItem(3, "Apple", 52.0, appleNutrients, "Fruits"));
        
        Map<String, Double> breadNutrients = new HashMap<>();
        breadNutrients.put("Protein", 9.0);
        breadNutrients.put("Fat", 3.2);
        breadNutrients.put("Carbs", 49.0);
        breadNutrients.put("Fiber", 2.7);
        mockItems.add(new FoodItem(1, "White bread", 265.0, breadNutrients, "Grains"));
        
        return mockItems;
    }

    private void showDatePicker() {
        // Create a simple date picker dialog
        JDialog dateDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dateDialog.setLayout(new BorderLayout());
        dateDialog.setSize(300, 200);
        dateDialog.setLocationRelativeTo(this);

        // Year dropdown (current year and past 5 years for meal dates)
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[6];
        for (int i = 0; i < 6; i++) {
            years[i] = String.valueOf(currentYear - 5 + i);
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
                
                // Allow any date selection (removed future date restriction)
                
                selectedDate = newDate;
                dateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                dateDialog.dispose();
                
                // Reload meals for the new date
                tableModel.setRowCount(0); // Clear current table
                loadTodaysMeals(); // Load meals for the new date
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
        
        // Create a custom dialog for food selection
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Food", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        // ===== Top Panel: Meal Type Selection =====
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        JComboBox<String> mealTypeCombo = new JComboBox<>(new String[]{"Breakfast", "Lunch", "Dinner", "Snack"});
        topPanel.add(new JLabel("Meal Type:"), BorderLayout.WEST);
        topPanel.add(mealTypeCombo, BorderLayout.CENTER);
        
        // ===== Search Panel =====
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search for food:"), BorderLayout.NORTH);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        // ===== Results list =====
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> resultsList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(resultsList);
        scrollPane.setPreferredSize(new Dimension(450, 200));
        
        // ===== Quantity panel =====
        JPanel quantityPanel = new JPanel(new BorderLayout(5, 5));
        JTextField quantityField = new JTextField();
        quantityPanel.add(new JLabel("Quantity (g):"), BorderLayout.WEST);
        quantityPanel.add(quantityField, BorderLayout.CENTER);
        
        // ===== Buttons panel =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        // ===== Bottom panel =====
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(quantityPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // ===== Add components to dialog =====
        dialog.add(topPanel, BorderLayout.NORTH);
        
        // Create center panel to hold both search and scroll pane
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(centerPanel, BorderLayout.CENTER);
        
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(confirmButton);
        
        // ===== Initialize with recommended foods =====
        mealTypeCombo.addActionListener(e -> {
            String selectedMealType = (String) mealTypeCombo.getSelectedItem();
            MealType mealType = MealType.valueOf(selectedMealType.toUpperCase());
            showRecommendedFoods(mealType, foodNames, listModel);
        });
        
        // Initial load with current meal type
        String selectedMealType = (String) mealTypeCombo.getSelectedItem();
        MealType mealType = MealType.valueOf(selectedMealType.toUpperCase());
        showRecommendedFoods(mealType, foodNames, listModel);
        
        // ===== Search functionality =====
        searchButton.addActionListener(e -> performSearch(searchField.getText(), foodNames, listModel, mealTypeCombo));
        searchField.addActionListener(e -> performSearch(searchField.getText(), foodNames, listModel, mealTypeCombo));
        
        // ===== Confirm button =====
        confirmButton.addActionListener(e -> {
            String selectedFood = resultsList.getSelectedValue();
            String quantity = quantityField.getText();
            String mealTypeStr = (String) mealTypeCombo.getSelectedItem();
            if (selectedFood != null && !quantity.isEmpty()) {
                // Format quantity to always show decimal
                double quantityValue = Double.parseDouble(quantity);
                String formattedQuantity = String.format("%.1f", quantityValue);
                
                // Format meal type to be consistent (first letter uppercase, rest lowercase)
                String formattedMealType = mealTypeStr.substring(0, 1).toUpperCase() + mealTypeStr.substring(1).toLowerCase();
                
                // Save to database immediately
                try {
                    int foodId = findFoodIdByName(selectedFood);
                    if (foodId != -1) {
                        model.meal.IngredientEntry ingredient = new model.meal.IngredientEntry(foodId, quantityValue);
                        List<model.meal.IngredientEntry> ingredients = new ArrayList<>();
                        ingredients.add(ingredient);
                        
                        model.meal.MealType mealTypeEnum = model.meal.MealType.valueOf(mealTypeStr.toUpperCase());
                        model.meal.Meal meal = new model.meal.Meal(
                            0, // MealID will be generated by database
                            currentUserId,
                            selectedDate,
                            mealTypeEnum,
                            ingredients
                        );
                        
                        if (databaseAdapter != null && databaseAdapter.getConnection() != null) {
                            databaseAdapter.saveMeal(meal);
                            System.out.println("Saved meal: " + selectedFood + " (" + formattedQuantity + "g, " + formattedMealType + ")");
                            
                            // Add to table with the new meal ID (we'll need to get it back)
                            // For now, add with empty meal ID and reload meals to get the correct IDs
                            tableModel.addRow(new Object[]{selectedFood, formattedQuantity, formattedMealType, 0.0, ""}); // Calories will be calculated later
                            
                            // Reload today's meals to get the correct meal IDs
                            loadTodaysMeals();
                            updateCaloriesDisplay(); // Update calories after adding a new row
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error saving meal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a food and enter a quantity.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // Cancel button
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }
    
    private void performSearch(String searchText, String[] foodNames, DefaultListModel<String> listModel, JComboBox<String> mealTypeCombo) {
        String query = searchText.toLowerCase().trim();
        MealType mealType = MealType.valueOf((String) mealTypeCombo.getSelectedItem());
        
        if (query.isEmpty()) {
            // If search box is empty, show initial list
            showInitialItems(foodNames, listModel);
            return;
        }
        
        // Search for matching foods
        List<String> matchingFoods = new ArrayList<>();
        for (String item : foodNames) {
            if (item.toLowerCase().contains(query)) {
                matchingFoods.add(item);
            }
        }
        
        // Add matching foods
        int count = 0;
        for (String item : matchingFoods) {
            if (count < 50) { // Limit display count
                listModel.addElement(item);
                count++;
            }
        }
        
        System.out.println("Total matches found: " + count);
        
        if (listModel.size() == 0) {
            listModel.addElement("No items found matching: " + searchText);
        }
    }
    
    private void showInitialItems(String[] foodNames, DefaultListModel<String> listModel) {
        listModel.clear();
        int maxItems = Math.min(foodNames.length, 50);
        for (int i = 0; i < maxItems; i++) {
            listModel.addElement(foodNames[i]);
        }
    }

    // OPTIONAL: for integration
    public Date getSelectedDate() {
        return java.sql.Date.valueOf(selectedDate);
    }
    
    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        updateDateDisplay();
        loadTodaysMeals(); // Reload meals for the new date
    }
    
    private void updateDateDisplay() {
        if (dateLabel != null) {
            dateLabel.setText("Date: " + selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
    }

    public String getMealType() {
        return (String) mealTypeBox.getSelectedItem();
    }

    public DefaultTableModel getIngredientData() {
        return tableModel;
    }
    

    
    private int findFoodIdByName(String foodName) {
        if (foodItems != null) {
            for (model.FoodItem food : foodItems) {
                if (food.getName().equals(foodName)) {
                    return food.getFoodID();
                }
            }
        }
        return -1; // Not found
    }
    
    private void loadTodaysMeals() {
        // Clear the table first to prevent duplicates
        tableModel.setRowCount(0);
        
        if (databaseAdapter != null && databaseAdapter.getConnection() != null) {
            try {
                // Load meals for current user and today's date
                List<model.meal.Meal> todaysMeals = databaseAdapter.loadMeals(currentUserId);
                
                // Filter meals for today's date
                List<model.meal.Meal> filteredMeals = todaysMeals.stream()
                    .filter(meal -> meal.getDate().equals(selectedDate))
                    .toList();
                
                // Create a list to hold all ingredients with their meal types for sorting
                List<Object[]> ingredientsWithMealType = new ArrayList<>();
                
                // Collect all ingredients with their meal types
                for (model.meal.Meal meal : filteredMeals) {
                    for (model.meal.IngredientEntry ingredient : meal.getIngredients()) {
                        // Find food name by ID
                        String foodName = findFoodNameById(ingredient.getFoodID());
                        if (foodName != null) {
                            // Format quantity to always show decimal
                            String formattedQuantity = String.format("%.1f", ingredient.getQuantity());
                            
                            // Format meal type to be consistent
                            String mealTypeName = meal.getType().name();
                            String formattedMealType = mealTypeName.substring(0, 1).toUpperCase() + mealTypeName.substring(1).toLowerCase();
                            
                            // Add to list for sorting
                            ingredientsWithMealType.add(new Object[]{foodName, formattedQuantity, formattedMealType, 0.0, meal.getMealID()});
                        }
                    }
                }
                
                // Sort by meal type: Breakfast, Lunch, Dinner, Snack
                ingredientsWithMealType.sort((a, b) -> {
                    String mealTypeA = (String) a[2];
                    String mealTypeB = (String) b[2];
                    
                    // Define meal type order
                    Map<String, Integer> mealTypeOrder = Map.of(
                        "Breakfast", 1,
                        "Lunch", 2,
                        "Dinner", 3,
                        "Snack", 4
                    );
                    
                    int orderA = mealTypeOrder.getOrDefault(mealTypeA, 5);
                    int orderB = mealTypeOrder.getOrDefault(mealTypeB, 5);
                    
                    return Integer.compare(orderA, orderB);
                });
                
                // Add sorted ingredients to table
                for (Object[] ingredient : ingredientsWithMealType) {
                    tableModel.addRow(ingredient);
                }
                
                // Update calories display after loading meals
                updateCaloriesDisplay();
                
            } catch (Exception e) {
                System.err.println("Error loading today's meals: " + e.getMessage());
            }
        }
    }
    
    private String findFoodNameById(int foodId) {
        if (foodItems != null) {
            for (model.FoodItem food : foodItems) {
                if (food.getFoodID() == foodId) {
                    return food.getName();
                }
            }
        }
        return null; // Not found
    }

    // Show recommended foods
    private void showRecommendedFoods(MealType mealType, String[] foodNames, DefaultListModel<String> listModel) {
        listModel.clear();
        
        // Add all foods without any recommendations
        for (String foodName : foodNames) {
            listModel.addElement(foodName);
        }
    }
}
