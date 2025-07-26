package view;

import controller.MealLoggerController;
import controller.UserProfileController;
import model.Goal;
import model.SwapSuggestion;
import model.meal.Meal;
import model.meal.MealType;
import chart.SwingChart;
import backend.SwapEngine;
import service.SwapService;
import service.SwapService.NutritionComparison;
import service.SwapService.NutrientChange;
import view.MealEntryPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Map;

public class JournalPanel extends JPanel {
    private JTable table;
    private MealLoggerController controller = new MealLoggerController();
    private UserProfileController userController = new UserProfileController();
    private SwapService swapService = new SwapService();
    private List<Meal> meals;
    private DefaultTableModel tableModel;
    private int userId;
    
    // Mock goals for demonstration - in real app, get from user profile
    private List<Goal> userGoals;
    
    // Unified color scheme
    private static final Color[] CHART_COLORS = {
        new Color(52, 152, 219),   // Blue
        new Color(231, 76, 60),    // Red
        new Color(46, 204, 113),   // Green
        new Color(155, 89, 182),   // Purple
        new Color(241, 196, 15),   // Yellow
        new Color(230, 126, 34),   // Orange
        new Color(26, 188, 156),   // Cyan
        new Color(149, 165, 166),  // Gray
        new Color(142, 68, 173),   // Dark Purple
        new Color(211, 84, 0)      // Dark Orange
    };

    public JournalPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Load user's actual goals from database
        loadUserGoals();
        
        // Initialize components
        initializeComponents();
        
        // Load today's meals
        loadTodaysMeals();
        
        // Populate the table
        populateDailySummaryTable();
    }
    
    private void loadUserGoals() {
        try {
            // Use GoalDAO to load real goals
            dao.Implementations.GoalDAO goalDAO = new dao.Implementations.GoalDAO();
            userGoals = goalDAO.loadGoals(userId);
            
            if (userGoals == null || userGoals.isEmpty()) {
                // Fallback to default goals if none set
                userGoals = Arrays.asList(
                    new Goal("Fiber", "Increase", 5.0, "moderate"),
                    new Goal("Calories", "Decrease", 200.0, "low")
                );
                System.out.println("No goals found for user " + userId + ", using default goals");
            } else {
                System.out.println("Loaded " + userGoals.size() + " goals for user " + userId);
            }
        } catch (Exception e) {
            System.err.println("Error loading user goals: " + e.getMessage());
            // Fallback to default goals
            userGoals = Arrays.asList(
                new Goal("Fiber", "Increase", 5.0, "moderate"),
                new Goal("Calories", "Decrease", 200.0, "low")
            );
        }
    }

    private void initializeComponents() {
        JLabel titleLabel = new JLabel("Your Daily Nutrition Summary", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Top panel with only 'Back to Main Menu' button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton backToMainBtn = new JButton("Back to Main Menu");
        backToMainBtn.addActionListener(e -> {
            // Close current window and return to main menu
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
        });
        
        topPanel.add(backToMainBtn);
        add(topPanel, BorderLayout.SOUTH);

        // Table setup - Daily summary view
        String[] columns = {"Date", "Total Calories", "Target Calories", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        // Meals will be loaded in loadTodaysMeals() method
        // Don't call populateDailySummaryTable() here as it's called in constructor

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                showDailyMealDetails(table.getSelectedRow());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTodaysMeals() {
        // Load all meals for the user
        this.meals = controller.getMealsForUser(userId);
        if (this.meals == null) {
            this.meals = new ArrayList<>();
        }
        
        System.out.println("Journal: Total meals for user: " + this.meals.size());
        
        // Debug: Show all meal dates
        LocalDate today = LocalDate.now();
        System.out.println("Journal: Today's date = " + today);
        for (Meal meal : this.meals) {
            System.out.println("Journal: Meal ID " + meal.getMealID() + " date: " + meal.getDate());
        }
        
        // Show all meals, not just today's
        // This allows users to see their meal history
        if (!this.meals.isEmpty()) {
            System.out.println("Journal: Showing all meals for user");
        }
    }

    private void populateDailySummaryTable() {
        tableModel.setRowCount(0);
        
        // Get food database for calorie calculation
        Map<Integer, model.FoodItem> foodDatabase = getFoodDatabase();
        
        // Group meals by date
        Map<LocalDate, List<Meal>> mealsByDate = new HashMap<>();
        for (Meal meal : meals) {
            mealsByDate.computeIfAbsent(meal.getDate(), k -> new ArrayList<>()).add(meal);
        }
        
        // Calculate daily totals and create table rows
        for (Map.Entry<LocalDate, List<Meal>> entry : mealsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Meal> dailyMeals = entry.getValue();
            
            double totalCalories = dailyMeals.stream()
                .mapToDouble(meal -> meal.getCalories(foodDatabase))
                .sum();
            
            // Mock target calories - in real app, get from user profile
            double targetCalories = 2000.0;
            
            String status = totalCalories <= targetCalories ? "On Track" : "Over Target";
            
            tableModel.addRow(new Object[]{
                date.toString(),
                String.format("%.0f", totalCalories),
                String.format("%.0f", targetCalories),
                status
            });
        }
    }

    private void showDailyMealDetails(int rowIndex) {
        String dateStr = (String) tableModel.getValueAt(rowIndex, 0);
        LocalDate selectedDate = LocalDate.parse(dateStr);
        
        // Get meals for this date
        List<Meal> dailyMeals = meals.stream()
            .filter(meal -> meal.getDate().equals(selectedDate))
            .toList();
        
        if (dailyMeals.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No meals recorded for " + dateStr, 
                "No Data", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create detailed view dialog
        JDialog detailDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Meal Details for " + dateStr, true);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setSize(1000, 700);
        detailDialog.setLocationRelativeTo(this);
        
        // Create main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left panel: Meal breakdown
        JPanel leftPanel = createMealBreakdownPanel(dailyMeals);
        
        // Right panel: Charts and swap suggestions
        JPanel rightPanel = createChartsAndSwapPanel(dailyMeals);
        
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(rightPanel, BorderLayout.CENTER);
        
        detailDialog.add(contentPanel, BorderLayout.CENTER);
        
        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> detailDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeBtn);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add window listener to clear table selection when dialog closes
        detailDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                // Clear table selection so the same row can be clicked again
                table.clearSelection();
            }
        });
        
        detailDialog.setVisible(true);
    }
    
    private JPanel createMealBreakdownPanel(List<Meal> dailyMeals) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Meal Breakdown"));
        panel.setPreferredSize(new Dimension(400, 400));
        
        // Create a simple table to show meals
        String[] columns = {"Meal Type", "Food Item", "Quantity (g)", "Calories"};
        DefaultTableModel mealModel = new DefaultTableModel(columns, 0);
        
        Map<Integer, model.FoodItem> foodDatabase = getFoodDatabase();
        
        for (Meal meal : dailyMeals) {
            if (meal.getIngredients() != null) {
                for (var ingredient : meal.getIngredients()) {
                    int foodId = ingredient.getFoodID();
                    double quantity = ingredient.getQuantity();
                    model.FoodItem food = foodDatabase.get(foodId);
                    
                    if (food != null) {
                        String foodName = food.getName();
                        double caloriesPer100g = food.getCalories();
                        double actualCalories = (caloriesPer100g * quantity) / 100.0;
                        
                        mealModel.addRow(new Object[]{
                            meal.getType().toString(),
                            foodName,
                            String.format("%.1f", quantity),
                            String.format("%.0f", actualCalories)
                        });
                    }
                }
            }
        }
        
        JTable mealTable = new JTable(mealModel);
        mealTable.setRowHeight(25);
        mealTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(mealTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createChartsAndSwapPanel(List<Meal> dailyMeals) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Daily Nutrition Analysis"));
        panel.setPreferredSize(new Dimension(500, 500));
        
        // Create tabbed pane for different charts
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1: Nutrient Distribution (Use Case 6)
        JPanel nutrientPanel = createNutrientDistributionPanel(dailyMeals);
        tabbedPane.addTab("Nutrients", nutrientPanel);
        
        // Tab 2: Food Groups (Use Case 7)
        JPanel foodGroupPanel = createFoodGroupPanel(dailyMeals);
        tabbedPane.addTab("Food Groups", foodGroupPanel);
        
        // Tab 3: Calorie Summary
        JPanel caloriePanel = createCalorieSummaryPanel(dailyMeals);
        tabbedPane.addTab("Calories", caloriePanel);
        
        // Tab 4: Swap Suggestions (Use Case 3-5)
        JPanel swapPanel = createSwapSuggestionsPanel(dailyMeals);
        tabbedPane.addTab("Swap Suggestions", swapPanel);
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createNutrientDistributionPanel(List<Meal> dailyMeals) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Calculate nutrient distribution
        Map<String, Double> nutrients = calculateDailyNutrients(dailyMeals);
        
        // Create tabbed pane for different chart types
        JTabbedPane chartTabs = new JTabbedPane();
        
        // Tab 1: Pie Chart
        JPanel pieChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth() - 40;
                int height = getHeight() - 120;
                int centerX = width / 2 + 20;
                int centerY = height / 2 + 20;
                int radius = Math.min(width, height) / 2 - 20;
                
                // Only show three major nutrients, filter out 0% items
                Map<String, Double> displayNutrients = new HashMap<>();
                for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
                    if (entry.getValue() > 0.1) { // Only show nutrients greater than 0.1%
                        displayNutrients.put(entry.getKey(), entry.getValue());
                    }
                }
                
                // Calculate total
                double total = displayNutrients.values().stream().mapToDouble(Double::doubleValue).sum();
                
                // Draw pie chart
                double currentAngle = 0;
                int colorIndex = 0;
                
                for (Map.Entry<String, Double> entry : displayNutrients.entrySet()) {
                    double percentage = entry.getValue() / total;
                    double arcAngle = percentage * 360;
                    
                    g2d.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                    g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 
                               (int)currentAngle, (int)arcAngle);
                    
                    currentAngle += arcAngle;
                    colorIndex++;
                }
                
                // Draw legend
                int legendY = height + 40;
                int legendX = 20;
                colorIndex = 0;
                
                for (Map.Entry<String, Double> entry : displayNutrients.entrySet()) {
                    double percentage = entry.getValue() / total;
                    
                    g2d.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                    g2d.fillRect(legendX, legendY, 15, 15);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(legendX, legendY, 15, 15);
                    
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    String label = entry.getKey() + " (" + String.format("%.1f%%", percentage * 100) + ")";
                    g2d.drawString(label, legendX + 20, legendY + 12);
                    
                    legendY += 20;
                    if (legendY > getHeight() - 20) {
                        legendX += 150;
                        legendY = height + 40;
                    }
                    colorIndex++;
                }
                
                // Draw title
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Daily Nutrient Distribution (Pie Chart)", 20, 20);
            }
        };
        
        // Tab 2: Bar Chart
        JPanel barChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth() - 60;
                int height = getHeight() - 80;
                int x = 40;
                int y = 20;
                
                // Find max value
                double maxValue = nutrients.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
                
                // Draw bars
                int barWidth = Math.max(30, width / nutrients.size() - 10);
                int currentX = x;
                int colorIndex = 0;
                
                for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
                    int barHeight = (int) ((entry.getValue() / maxValue) * height);
                    
                    g2d.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                    g2d.fillRect(currentX, y + height - barHeight, barWidth, barHeight);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(currentX, y + height - barHeight, barWidth, barHeight);
                    
                    // Draw value on bar
                    g2d.setFont(new Font("Arial", Font.BOLD, 9));
                    String valueText = String.format("%.1f", entry.getValue());
                    g2d.drawString(valueText, currentX + 2, y + height - barHeight - 5);
                    
                    // Draw label
                    g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                    String label = entry.getKey();
                    if (label.length() > 8) {
                        label = label.substring(0, 8) + "...";
                    }
                    g2d.drawString(label, currentX, y + height + 15);
                    
                    currentX += barWidth + 10;
                    colorIndex++;
                }
                
                // Draw title and axis
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Daily Nutrient Distribution (Bar Chart)", 20, 20);
                
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                g2d.drawString("Nutrients", width/2, height + 50);
            }
        };
        
        pieChartPanel.setPreferredSize(new Dimension(400, 350));
        barChartPanel.setPreferredSize(new Dimension(400, 350));
        
        chartTabs.addTab("Pie Chart", pieChartPanel);
        chartTabs.addTab("Bar Chart", barChartPanel);
        
        panel.add(chartTabs, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFoodGroupPanel(List<Meal> dailyMeals) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Calculate food group distribution
        Map<String, Double> foodGroups = calculateFoodGroupDistribution(dailyMeals);
        
        // Create bar chart panel
        JPanel barChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth() - 40;
                int height = getHeight() - 60;
                int x = 20;
                int y = 20;
                
                // Find max value
                double maxValue = foodGroups.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
                
                // Draw bars
                int barWidth = width / foodGroups.size() - 10;
                int currentX = x;
                int colorIndex = 0;
                
                for (Map.Entry<String, Double> entry : foodGroups.entrySet()) {
                    int barHeight = (int) ((entry.getValue() / maxValue) * height);
                    
                    g2d.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                    g2d.fillRect(currentX, y + height - barHeight, barWidth, barHeight);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(currentX, y + height - barHeight, barWidth, barHeight);
                    
                    // Draw label
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 10));
                    g2d.drawString(entry.getKey(), currentX, y + height + 15);
                    g2d.drawString(String.format("%.1fg", entry.getValue()), currentX, y + height + 30);
                    
                    currentX += barWidth + 10;
                    colorIndex++;
                }
                
                // Draw title
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Food Group Distribution", 20, 15);
            }
        };
        
        barChartPanel.setPreferredSize(new Dimension(300, 250));
        panel.add(barChartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCalorieSummaryPanel(List<Meal> dailyMeals) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Calculate total calories
        Map<Integer, model.FoodItem> foodDatabase = getFoodDatabase();
        double totalCalories = dailyMeals.stream()
            .mapToDouble(meal -> meal.getCalories(foodDatabase))
            .sum();
        
        // Create summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        
        JLabel caloriesLabel = new JLabel("Total Calories: " + String.format("%.0f", totalCalories));
        caloriesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        summaryPanel.add(caloriesLabel);
        
        JLabel targetLabel = new JLabel("Target: 2000 calories");
        targetLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        summaryPanel.add(targetLabel);
        
        String status = totalCalories <= 2000 ? "On Track" : "Over Target";
        JLabel statusLabel = new JLabel("Status: " + status);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryPanel.add(statusLabel);
        
        JLabel mealsLabel = new JLabel("Meals: " + dailyMeals.size() + " recorded");
        mealsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        summaryPanel.add(mealsLabel);
        
        panel.add(summaryPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private Map<String, Double> calculateFoodGroupDistribution(List<Meal> dailyMeals) {
        Map<String, Double> foodGroups = new HashMap<>();
        Map<Integer, model.FoodItem> foodDatabase = getFoodDatabase();
        
        for (Meal meal : dailyMeals) {
            for (model.meal.IngredientEntry ingredient : meal.getIngredients()) {
                model.FoodItem food = foodDatabase.get(ingredient.getFoodID());
                if (food != null) {
                    String group = food.getFoodGroup();
                    double quantity = ingredient.getQuantity();
                    foodGroups.merge(group, quantity, Double::sum);
                }
            }
        }
        
        return foodGroups;
    }
    
    private Map<String, Double> calculateDailyNutrients(List<Meal> dailyMeals) {
        Map<String, Double> allNutrients = new HashMap<>();
        
        // Use real nutrition analyzer to calculate from ingredients
        backend.NutritionAnalyzer analyzer = new backend.NutritionAnalyzer(getFoodDatabase());
        
        for (Meal meal : dailyMeals) {
            Map<String, Double> mealNutrients = analyzer.analyzeMeal(meal);
            for (Map.Entry<String, Double> entry : mealNutrients.entrySet()) {
                allNutrients.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        
        // According to professor's guidance: display carbs, calories, iron, proteins, fats, sugars, some vitamins, fibers
        Map<String, Double> cleanedNutrients = new HashMap<>();
        
        // Extract professor's suggested core nutrients
        double fatGrams = 0.0;
        double proteinGrams = 0.0;
        double carbGrams = 0.0;
        double sugarGrams = 0.0;
        double fiberGrams = 0.0;
        double ironMg = 0.0;
        double vitaminCMg = 0.0;
        double caloriesKcal = 0.0;
        
        // Other nutrients (keep original units)
        Map<String, Double> otherNutrients = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : allNutrients.entrySet()) {
            String nutrientName = entry.getKey();
            double value = entry.getValue();
            
            // Match professor's suggested core nutrients
            if (nutrientName.toUpperCase().contains("FAT (TOTAL LIPIDS)") || nutrientName.toUpperCase().contains("FAT") || nutrientName.toUpperCase().contains("LIPIDS")) {
                fatGrams += value;
                System.out.println("FAT FOUND: " + nutrientName + " = " + value + "g");
            } else if (nutrientName.toUpperCase().contains("PROTEIN")) {
                proteinGrams += value;
                System.out.println("PROTEIN FOUND: " + nutrientName + " = " + value + "g");
            } else if (nutrientName.toUpperCase().contains("CARBOHYDRATE, TOTAL") || nutrientName.toUpperCase().contains("CARBOHYL") || 
                       nutrientName.toUpperCase().contains("STARCH")) {
                carbGrams += value;
                System.out.println("CARBS FOUND: " + nutrientName + " = " + value + "g");
            } else if (nutrientName.toUpperCase().contains("SUGARS, TOTAL") || nutrientName.toUpperCase().contains("SUGARS, T")) {
                sugarGrams += value;
                System.out.println("SUGAR FOUND: " + nutrientName + " = " + value + "g");
            } else if (nutrientName.toUpperCase().contains("FIBRE, TOTAL") || nutrientName.toUpperCase().contains("FIBRE, TOT")) {
                fiberGrams += value;
                System.out.println("FIBER FOUND: " + nutrientName + " = " + value + "g");
            } else if (nutrientName.toUpperCase().contains("IRON")) {
                ironMg += value;
                System.out.println("IRON FOUND: " + nutrientName + " = " + value + "mg");
            } else if (nutrientName.toUpperCase().contains("VITAMIN C")) {
                vitaminCMg += value;
                System.out.println("VITAMIN C FOUND: " + nutrientName + " = " + value + "mg");
            } else if (nutrientName.toUpperCase().contains("ENERGY (KILOCALORIES)") || nutrientName.toUpperCase().contains("KCAL")) {
                caloriesKcal += value;
                System.out.println("CALORIES FOUND: " + nutrientName + " = " + value + "kcal");
            } else {
                // Other nutrients keep original values
                otherNutrients.put(nutrientName, value);
            }
        }
        
        // Convert to calorie contribution (three major nutrients)
        double fatKcal = fatGrams * 9;  // 1g fat = 9kcal
        double proteinKcal = proteinGrams * 4;  // 1g protein = 4kcal  
        double carbKcal = carbGrams * 4;  // 1g carbohydrate = 4kcal
        
        double totalKcal = fatKcal + proteinKcal + carbKcal;
        
        System.out.println("=== Nutrient Summary ===");
        System.out.println("Fat: " + fatGrams + "g = " + fatKcal + "kcal");
        System.out.println("Protein: " + proteinGrams + "g = " + proteinKcal + "kcal");
        System.out.println("Carbohydrates: " + carbGrams + "g = " + carbKcal + "kcal");
        System.out.println("Sugar: " + sugarGrams + "g");
        System.out.println("Fiber: " + fiberGrams + "g");
        System.out.println("Iron: " + ironMg + "mg");
        System.out.println("Vitamin C: " + vitaminCMg + "mg");
        System.out.println("Total Calories: " + caloriesKcal + "kcal");
        
        // Calculate percentage of three major nutrients (based on calorie contribution)
        if (totalKcal > 0) {
            cleanedNutrients.put("Fat", (fatKcal / totalKcal) * 100);
            cleanedNutrients.put("Protein", (proteinKcal / totalKcal) * 100);
            cleanedNutrients.put("Carbohydrates", (carbKcal / totalKcal) * 100);
        }
        
        // Add other core nutrients (keep original units)
        if (sugarGrams > 0) cleanedNutrients.put("Sugar", sugarGrams);
        if (fiberGrams > 0) cleanedNutrients.put("Fiber", fiberGrams);
        if (ironMg > 0) cleanedNutrients.put("Iron", ironMg);
        if (vitaminCMg > 0) cleanedNutrients.put("Vitamin C", vitaminCMg);
        if (caloriesKcal > 0) cleanedNutrients.put("Calories", caloriesKcal);
        
        // Display final data
        System.out.println("=== Final Pie Chart Data ===");
        for (Map.Entry<String, Double> entry : cleanedNutrients.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + (entry.getKey().equals("Calories") ? " kcal" : entry.getKey().equals("Iron") || entry.getKey().equals("Vitamin C") ? " mg" : "g"));
        }
        
        return cleanedNutrients;
    }
    
    private Map<Integer, model.FoodItem> getFoodDatabase() {
        // Load real food database from MySQL
        Map<Integer, model.FoodItem> foodDatabase = new HashMap<>();
        
        try {
            dao.adapter.MySQLAdapter adapter = new dao.adapter.MySQLAdapter();
            adapter.connect();
            List<model.FoodItem> foods = adapter.loadFoods();
            
            for (model.FoodItem food : foods) {
                foodDatabase.put(food.getFoodID(), food);
            }
            
            System.out.println("SUCCESS: Loaded " + foods.size() + " food items from DATABASE for nutrition calculation");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load food database: " + e.getMessage());
            System.err.println("WARNING: Falling back to MOCK data - this means nutrition calculations may be inaccurate!");
            
            // Fallback to mock data if database fails
            foodDatabase.put(1, new model.FoodItem(1, "Beef Steak", 250, 
                Map.of("Calories", 250.0, "Protein", 26.0, "Fat", 15.0, "Sodium", 70.0), "Meat"));
            foodDatabase.put(2, new model.FoodItem(2, "Chicken Breast", 165, 
                Map.of("Calories", 165.0, "Protein", 31.0, "Fat", 3.6, "Sodium", 74.0), "Meat"));
            foodDatabase.put(3, new model.FoodItem(3, "Salmon", 208, 
                Map.of("Calories", 208.0, "Protein", 25.0, "Fat", 12.0, "Omega-3", 2.3), "Fish"));
            foodDatabase.put(4, new model.FoodItem(4, "Brown Rice", 111, 
                Map.of("Calories", 111.0, "Protein", 2.6, "Fiber", 1.8, "Carbs", 23.0), "Grains"));
            foodDatabase.put(5, new model.FoodItem(5, "White Rice", 130, 
                Map.of("Calories", 130.0, "Protein", 2.7, "Fiber", 0.4, "Carbs", 28.0), "Grains"));
            foodDatabase.put(6, new model.FoodItem(6, "Broccoli", 55, 
                Map.of("Calories", 55.0, "Protein", 3.7, "Fiber", 5.2, "Vitamin C", 89.0), "Vegetables"));
            foodDatabase.put(7, new model.FoodItem(7, "Spinach", 23, 
                Map.of("Calories", 23.0, "Protein", 2.9, "Fiber", 2.2, "Iron", 2.7), "Vegetables"));
            foodDatabase.put(8, new model.FoodItem(8, "Apple", 95, 
                Map.of("Calories", 95.0, "Protein", 0.5, "Fiber", 4.4, "Vitamin C", 8.4), "Fruits"));
            foodDatabase.put(9, new model.FoodItem(9, "Banana", 105, 
                Map.of("Calories", 105.0, "Protein", 1.3, "Fiber", 3.1, "Potassium", 422.0), "Fruits"));
            foodDatabase.put(10, new model.FoodItem(10, "Greek Yogurt", 59, 
                Map.of("Calories", 59.0, "Protein", 10.0, "Fat", 0.4, "Calcium", 110.0), "Dairy"));
            
            // Add more common foods
            foodDatabase.put(11, new model.FoodItem(11, "Cheese souffle", 300, 
                Map.of("Calories", 300.0, "Protein", 15.0, "Fat", 25.0, "Carbs", 5.0), "Dairy"));
            foodDatabase.put(12, new model.FoodItem(12, "Bread", 265, 
                Map.of("Calories", 265.0, "Protein", 9.0, "Fat", 3.0, "Carbs", 49.0, "Fiber", 2.7), "Grains"));
            foodDatabase.put(13, new model.FoodItem(13, "Eggs", 155, 
                Map.of("Calories", 155.0, "Protein", 13.0, "Fat", 11.0, "Carbs", 1.1), "Protein"));
            foodDatabase.put(14, new model.FoodItem(14, "Milk", 42, 
                Map.of("Calories", 42.0, "Protein", 3.4, "Fat", 1.0, "Carbs", 5.0, "Calcium", 113.0), "Dairy"));
        }
        
        return foodDatabase;
    }
    
    private JPanel createNutritionChartPanel(Map<String, Double> nutrients) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // Create chart using SwingChart
        SwingChart chart = new SwingChart("Daily Nutrition Breakdown");
        chart.setData(nutrients);
        chart.render();
        
        panel.add(chart.getChartPanel(), BorderLayout.CENTER);
        
        // Add target comparison
        JPanel comparisonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        comparisonPanel.setBorder(BorderFactory.createTitledBorder("Target Comparison"));
        
        // Handle null values for nutrients
        Double calories = nutrients.get("Calories");
        Double fiber = nutrients.get("Fiber");
        
        String caloriesText = calories != null ? String.format("%.0f", calories) : "0";
        String fiberText = fiber != null ? String.format("%.1f", fiber) : "0.0";
        
        JLabel caloriesLabel = new JLabel(String.format("Calories: %s / 2000 (Target)", caloriesText));
        JLabel fiberLabel = new JLabel(String.format("Fiber: %s / 25.0 (Target)", fiberText));
        
        comparisonPanel.add(caloriesLabel);
        comparisonPanel.add(fiberLabel);
        
        panel.add(comparisonPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel createSwapSuggestionsPanel(List<Meal> dailyMeals) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Food Swap Suggestions"));
        
        // Get food database for suggestions
        Map<Integer, model.FoodItem> foodDatabase = getFoodDatabase();
        
        // Generate swap suggestions based on current meals and goals
        List<SwapSuggestion> allSuggestions = generateSmartSwapSuggestions(dailyMeals, foodDatabase);
        
        if (allSuggestions.isEmpty()) {
            JLabel noSuggestionsLabel = new JLabel("No swap suggestions available for today's meals.");
            noSuggestionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noSuggestionsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(noSuggestionsLabel, BorderLayout.CENTER);
        } else {
            // Create suggestions list
            JTextArea suggestionsArea = new JTextArea();
            suggestionsArea.setEditable(false);
            suggestionsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
            suggestionsArea.setLineWrap(true);
            suggestionsArea.setWrapStyleWord(true);

            StringBuilder sb = new StringBuilder();
            sb.append("Swap Suggestions for Better Nutrition:\n\n");
            
            for (int i = 0; i < allSuggestions.size(); i++) {
                SwapSuggestion suggestion = allSuggestions.get(i);
                
                // Get food names
                model.FoodItem originalFood = foodDatabase.get(suggestion.getOriginal().getFoodID());
                model.FoodItem replacementFood = foodDatabase.get(suggestion.getReplacement().getFoodID());
                
                String originalName = originalFood != null ? originalFood.getName() : "Unknown Food";
                String replacementName = replacementFood != null ? replacementFood.getName() : "Unknown Food";
                
                // Simplified format with nutrient changes
                sb.append(i + 1).append(". Replace: ").append(originalName).append(" (").append(suggestion.getOriginal().getQuantity()).append("g)\n");
                sb.append("   With: ").append(replacementName).append(" (").append(suggestion.getReplacement().getQuantity()).append("g)\n");
                
                // Extract nutrient changes from the reason (remove the "Replace X with Y to" part)
                String reason = suggestion.getReason();
                if (reason.contains(" to ")) {
                    String nutrientChanges = reason.substring(reason.indexOf(" to ") + 4);
                    sb.append("   ").append(nutrientChanges).append("\n\n");
                } else {
                    sb.append("\n");
                }
            }
            
            sb.append("Note: These suggestions will help you meet your nutrition goals.\n");
            sb.append("Click 'Apply All' to implement these changes in your database.");
            
            suggestionsArea.setText(sb.toString());
            
            JScrollPane scrollPane = new JScrollPane(suggestionsArea);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Add action buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton applyAllBtn = new JButton("Apply All Suggestions");
            JButton ignoreBtn = new JButton("Ignore All");
            
            applyAllBtn.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(panel, 
                    "This will modify your meal records in the database.\n" +
                    "Are you sure you want to apply all suggestions?",
                    "Confirm Changes", 
                    JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    applyAllSuggestions(dailyMeals, allSuggestions);
                    JOptionPane.showMessageDialog(panel, 
                        "All suggestions have been applied!\n" +
                        "Your meal records have been updated in the database.",
                        "Changes Applied", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            ignoreBtn.addActionListener(e -> {
                JOptionPane.showMessageDialog(panel, 
                    "Suggestions ignored. Your meal records remain unchanged.",
                    "No Changes Made", 
                    JOptionPane.INFORMATION_MESSAGE);
            });
            
            buttonPanel.add(applyAllBtn);
            buttonPanel.add(ignoreBtn);
            
            panel.add(buttonPanel, BorderLayout.SOUTH);
        }
        
        return panel;
    }
    
    // Generate smart swap suggestions based on current meals and user goals
    private List<SwapSuggestion> generateSmartSwapSuggestions(List<Meal> dailyMeals, Map<Integer, model.FoodItem> foodDatabase) {
        List<SwapSuggestion> allSuggestions = new ArrayList<>();
        Set<Integer> usedAlternatives = new HashSet<>();
        
        System.out.println("=== START SWAP DEBUG ===");
        
        // For saving suggested food IDs by meal type
        Map<model.meal.MealType, Integer> mealTypeSuggestions = new HashMap<>();
        
        for (Meal meal : dailyMeals) {
            List<SwapSuggestion> mealSuggestions = new ArrayList<>();
            
            for (model.meal.IngredientEntry ingredient : meal.getIngredients()) {
                model.FoodItem currentFood = foodDatabase.get(ingredient.getFoodID());
                if (currentFood == null) continue;
                
                SwapSuggestion suggestion = generateSmartSuggestion(currentFood, ingredient, foodDatabase, usedAlternatives);
                if (suggestion != null) {
                    mealSuggestions.add(suggestion);
                }
            }
            
            // Limit to maximum 1 suggestion per meal as per use case requirement
            if (mealSuggestions.size() > 1) {
                mealSuggestions.sort((a, b) -> {
                    boolean aUnused = !usedAlternatives.contains(a.getReplacement().getFoodID());
                    boolean bUnused = !usedAlternatives.contains(b.getReplacement().getFoodID());
                    if (aUnused != bUnused) {
                        return aUnused ? -1 : 1;
                    }
                    return 0;
                });
                mealSuggestions = mealSuggestions.subList(0, 1);
            }
            
            // Save suggestion for this meal type
            if (!mealSuggestions.isEmpty()) {
                SwapSuggestion bestSuggestion = mealSuggestions.get(0);
                mealTypeSuggestions.put(meal.getMealType(), bestSuggestion.getReplacement().getFoodID());
                System.out.println("Saved suggestion for " + meal.getMealType() + ": FoodID " + bestSuggestion.getReplacement().getFoodID());
            }
            
            allSuggestions.addAll(mealSuggestions);
        }
        
        // Save suggestions to MealEntryPanel for tomorrow's use
        view.MealEntryPanel.setYesterdaySwapSuggestions(mealTypeSuggestions);
        System.out.println("Saved " + mealTypeSuggestions.size() + " meal type suggestions to MealEntryPanel");
        
        System.out.println("Total suggestions generated: " + allSuggestions.size());
        System.out.println("=== END SWAP DEBUG ===");
        
        return allSuggestions;
    }
    
    // Generate smart suggestions based on user goals and nutrient data
    private SwapSuggestion generateSmartSuggestion(model.FoodItem currentFood, model.meal.IngredientEntry ingredient, 
                                                   Map<Integer, model.FoodItem> foodDatabase, Set<Integer> usedAlternatives) {
        if (userGoals == null || userGoals.isEmpty()) {
            return null;
        }
        
        // Get current food's nutrients
        Map<String, Double> currentNutrients = currentFood.getNutrients();
        if (currentNutrients == null) {
            return null;
        }
        
        // Collect all possible suggestions for all goals
        List<SwapSuggestion> allSuggestions = new ArrayList<>();
        
        // Try each goal
        for (Goal goal : userGoals) {
            String targetNutrient = goal.getNutrient().toLowerCase();
            String direction = goal.getDirection().toLowerCase();
            
            System.out.println("    Checking goal: " + goal.getNutrient() + " " + goal.getDirection());
            
            // Get current value for target nutrient
            double currentValue = getNutrientValue(currentNutrients, targetNutrient);
            System.out.println("    Current " + targetNutrient + " value: " + currentValue);
            
            // First, try to find unused alternatives
            SwapSuggestion bestUnusedSuggestion = findBestAlternative(currentFood, ingredient, foodDatabase, 
                usedAlternatives, goal, targetNutrient, direction, currentValue, currentNutrients, false);
            
            if (bestUnusedSuggestion != null) {
                allSuggestions.add(bestUnusedSuggestion);
            }
            
            // If no unused alternatives found, try used ones (allow repetition)
            SwapSuggestion bestUsedSuggestion = findBestAlternative(currentFood, ingredient, foodDatabase, 
                usedAlternatives, goal, targetNutrient, direction, currentValue, currentNutrients, true);
            
            if (bestUsedSuggestion != null) {
                allSuggestions.add(bestUsedSuggestion);
            }
        }
        
        // Return the best suggestion (prioritize unused alternatives, then by goal order)
        if (!allSuggestions.isEmpty()) {
            // Sort by priority: unused alternatives first, then by goal order
            allSuggestions.sort((a, b) -> {
                // First priority: unused alternatives
                boolean aUnused = !usedAlternatives.contains(a.getReplacement().getFoodID());
                boolean bUnused = !usedAlternatives.contains(b.getReplacement().getFoodID());
                if (aUnused != bUnused) {
                    return aUnused ? -1 : 1;
                }
                // Second priority: goal order (first goal has higher priority)
                return 0; // Keep original order
            });
            
            return allSuggestions.get(0);
        }
        
        return null;
    }
    
    // Helper method to find best alternative
        private SwapSuggestion findBestAlternative(model.FoodItem currentFood, model.meal.IngredientEntry ingredient,
                                              Map<Integer, model.FoodItem> foodDatabase, Set<Integer> usedAlternatives,
                                              Goal goal, String targetNutrient, String direction, 
                                              double currentValue, Map<String, Double> currentNutrients, boolean allowUsed) {
        
        model.FoodItem bestAlternative = null;
        double bestValue = direction.equals("increase") ? currentValue : Double.MAX_VALUE;
        double currentCalories = calculateCaloriesFromMacros(currentNutrients);
        
        // First try with ±10% calorie range
        bestAlternative = findAlternativeInCalorieRange(currentFood, foodDatabase, usedAlternatives, 
            goal, targetNutrient, direction, currentValue, currentCalories, 0.1, allowUsed);
        
        // If no alternative found with ±10%, try ±30%
        if (bestAlternative == null) {
            bestAlternative = findAlternativeInCalorieRange(currentFood, foodDatabase, usedAlternatives, 
                goal, targetNutrient, direction, currentValue, currentCalories, 0.3, allowUsed);
        }
        
        if (bestAlternative != null) {
            double altValue = getNutrientValue(bestAlternative.getNutrients(), targetNutrient);
            System.out.println("    Found best alternative: " + bestAlternative.getName());
            System.out.println("    Alternative " + targetNutrient + " value: " + altValue);
            System.out.println("    Alternative calories: " + calculateCaloriesFromMacros(bestAlternative.getNutrients()));
            
            String reason = generateReason(currentFood, bestAlternative, goal, currentValue, altValue);
            SwapSuggestion suggestion = new SwapSuggestion(ingredient, 
                new model.meal.IngredientEntry(bestAlternative.getFoodID(), ingredient.getQuantity()), 
                reason);
            
            // Mark this alternative as used
            usedAlternatives.add(bestAlternative.getFoodID());
            
            return suggestion;
        }
        
        return null;
    }
    
    private model.FoodItem findAlternativeInCalorieRange(model.FoodItem currentFood, 
                                                        Map<Integer, model.FoodItem> foodDatabase, 
                                                        Set<Integer> usedAlternatives,
                                                        Goal goal, String targetNutrient, String direction, 
                                                        double currentValue, double currentCalories, 
                                                        double calorieRange, boolean allowUsed) {
        
        model.FoodItem bestAlternative = null;
        double bestValue = direction.equals("increase") ? currentValue : Double.MAX_VALUE;
        int practicalFoodsChecked = 0;
        int totalFoodsChecked = 0;
        
        // Get current food's group
        String currentFoodGroup = currentFood.getFoodGroup();
        System.out.println("    Looking for alternatives in food group: " + currentFoodGroup);
        
        for (model.FoodItem alternative : foodDatabase.values()) {
            totalFoodsChecked++;
            if (alternative.getFoodID() == currentFood.getFoodID()) {
                continue; // Skip same food
            }
            
            // Check if we should skip used alternatives
            if (!allowUsed && usedAlternatives.contains(alternative.getFoodID())) {
                continue;
            }
            
            // 1. Food group filtering - according to use case requirements, prioritize search within same food group
            if (!currentFoodGroup.equals(alternative.getFoodGroup())) {
                continue; // Skip different food groups
            }
            
            // 2. Practicality filtering
            if (!isPracticalFood(alternative.getName())) {
                continue;
            }
            practicalFoodsChecked++;
            
            Map<String, Double> altNutrients = alternative.getNutrients();
            if (altNutrients == null) continue;
            
            double altValue = getNutrientValue(altNutrients, targetNutrient);
            double altCalories = calculateCaloriesFromMacros(altNutrients);
            
            // Skip if calories are 0 or too low (likely calculation error)
            if (altCalories <= 0) continue;
            
            // Check calorie range (±10% or ±30%) - according to use case requirements to keep other nutrients constant
            boolean inCalorieRange = altCalories >= currentCalories * (1 - calorieRange) && 
                                   altCalories <= currentCalories * (1 + calorieRange);
            
            if (!inCalorieRange) continue;
            
            // 3. Check if it violates any user goals - new strict check
            boolean violatesAnyGoal = false;
            if (userGoals != null) {
                for (Goal userGoal : userGoals) {
                    String goalNutrient = userGoal.getNutrient();
                    String goalDirection = userGoal.getDirection();
                    
                    // Skip the nutrient being optimized (main goal)
                    if (goalNutrient.equalsIgnoreCase(targetNutrient)) {
                        continue;
                    }
                    
                    double currentGoalValue = getNutrientValue(currentFood.getNutrients(), goalNutrient);
                    double altGoalValue = getNutrientValue(altNutrients, goalNutrient);
                    
                    // Check if it violates the goal direction
                    if (goalDirection.equalsIgnoreCase("increase") && altGoalValue < currentGoalValue) {
                        // Goal is to increase, but alternative food decreased
                        violatesAnyGoal = true;
                        break;
                    } else if (goalDirection.equalsIgnoreCase("decrease") && altGoalValue > currentGoalValue) {
                        // Goal is to decrease, but alternative food increased
                        violatesAnyGoal = true;
                        break;
                    }
                }
            }
            
            if (violatesAnyGoal) continue;
            
            // Check if alternative is better for the goal
            boolean isBetter = false;
            if (direction.equals("increase")) {
                isBetter = altValue > currentValue;
                if (isBetter && altValue > bestValue) {
                    bestValue = altValue;
                    bestAlternative = alternative;
                }
            } else if (direction.equals("decrease")) {
                isBetter = altValue < currentValue;
                if (isBetter && altValue < bestValue) {
                    bestValue = altValue;
                    bestAlternative = alternative;
                }
            }
        }
        
        // Debug information: show filtering effect
        System.out.println("    === Food Group Filtering Statistics ===");
        System.out.println("    Target Food Group: " + currentFoodGroup);
        System.out.println("    Total foods checked: " + totalFoodsChecked);
        System.out.println("    Foods in same group: " + practicalFoodsChecked);
        System.out.println("    Filtered out different groups: " + (totalFoodsChecked - practicalFoodsChecked));
        System.out.println("    Foods in same group ratio: " + String.format("%.1f%%", (double)practicalFoodsChecked / totalFoodsChecked * 100));
        System.out.println("    =================================");
        
        return bestAlternative;
    }
    
    // Check if food is practical for meal replacement
    private boolean isPracticalFood(String foodName) {
        String lowerName = foodName.toLowerCase();
        
        // 1. Filter out obviously impractical foods (spices, ingredients, etc.)
        if (lowerName.contains("spice") || lowerName.contains("curry") || 
            lowerName.contains("powder") || lowerName.contains("extract") ||
            lowerName.contains("concentrate") || lowerName.contains("bran") ||
            lowerName.contains("flour") || lowerName.contains("fungi") ||
            lowerName.contains("leaves") || lowerName.contains("vine") ||
            lowerName.contains("desiccated") || lowerName.contains("unsweetened") ||
            lowerName.contains("crude") || lowerName.contains("supplement") ||
            lowerName.contains("peel") || lowerName.contains("skin") ||
            lowerName.contains("essence") || lowerName.contains("flavor") ||
            lowerName.contains("seasoning") || lowerName.contains("herb") ||
            lowerName.contains("condiment") || lowerName.contains("sauce base")) {
            return false;
        }
        
        // Filter out raw meat/fish (except vegetables/fruits)
        if (lowerName.contains("raw") && 
            (lowerName.contains("meat") || lowerName.contains("fish") || 
             lowerName.contains("beef") || lowerName.contains("chicken") ||
             lowerName.contains("pork") || lowerName.contains("salmon"))) {
            return false;
        }
        
        // Filter out dried foods (except fruits)
        if (lowerName.contains("dried") && !lowerName.contains("fruit")) {
            return false;
        }
        
        // Filter out high-calorie nuts/seeds (Group 12)
        if (lowerName.contains("nut") || lowerName.contains("seed") ||
            lowerName.contains("almond") || lowerName.contains("walnut") ||
            lowerName.contains("peanut") || lowerName.contains("cashew")) {
            return false;
        }
        
        // Filter out pure fats/oils (Group 4) - only for cooking, not main meals
        if (lowerName.contains("oil") || lowerName.contains("butter") ||
            lowerName.contains("margarine") || lowerName.contains("lard")) {
            return false;
        }
        
        // 2. Must contain at least one practical food component (priority group)
        return lowerName.contains("chicken") || lowerName.contains("beef") || 
               lowerName.contains("pork") || lowerName.contains("fish") ||
               lowerName.contains("rice") || lowerName.contains("pasta") ||
               lowerName.contains("bread") || lowerName.contains("potato") ||
               lowerName.contains("vegetable") || lowerName.contains("fruit") ||
               lowerName.contains("egg") || lowerName.contains("milk") ||
               lowerName.contains("cheese") || lowerName.contains("soup") ||
               lowerName.contains("salad") || lowerName.contains("sandwich") ||
               lowerName.contains("noodle") || lowerName.contains("bean") ||
               lowerName.contains("pea") || lowerName.contains("corn") ||
               lowerName.contains("tomato") || lowerName.contains("onion") ||
               lowerName.contains("carrot") || lowerName.contains("broccoli") ||
               lowerName.contains("apple") || lowerName.contains("banana") ||
               lowerName.contains("orange") || lowerName.contains("grape") ||
               lowerName.contains("steak") || lowerName.contains("burger") ||
               lowerName.contains("pizza") || lowerName.contains("lasagna") ||
               lowerName.contains("stew") || lowerName.contains("casserole") ||
               lowerName.contains("pie") || lowerName.contains("cake") ||
               lowerName.contains("yogurt") || lowerName.contains("sauce") ||
               lowerName.contains("sausage") || lowerName.contains("ham") ||
               lowerName.contains("turkey") || lowerName.contains("salmon") ||
               lowerName.contains("tuna") || lowerName.contains("shrimp") ||
               lowerName.contains("lentil") || lowerName.contains("chickpea") ||
               lowerName.contains("oat") || lowerName.contains("wheat") ||
               lowerName.contains("barley") || lowerName.contains("quinoa") ||
               lowerName.contains("meal") || lowerName.contains("dish") ||
               lowerName.contains("entree") || lowerName.contains("main") ||
               lowerName.contains("dinner") || lowerName.contains("lunch") ||
               lowerName.contains("breakfast") || lowerName.contains("snack") ||
               lowerName.contains("wrap") || lowerName.contains("roll") ||
               lowerName.contains("taco") || lowerName.contains("burrito") ||
               lowerName.contains("curry") || lowerName.contains("stir") ||
               lowerName.contains("roast") || lowerName.contains("grill") ||
               lowerName.contains("bake") || lowerName.contains("fried") ||
               lowerName.contains("boil") || lowerName.contains("steam");
    }
    
    // Get nutrient value with proper name matching
    private double getNutrientValue(Map<String, Double> nutrients, String targetNutrient) {
        if (nutrients == null) return 0.0;
        
        // Handle different nutrient name variations
        for (String key : nutrients.keySet()) {
            String lowerKey = key.toLowerCase();
            String lowerTarget = targetNutrient.toLowerCase();
            
            if (lowerTarget.equals("calories") || lowerTarget.equals("energy")) {
                if (lowerKey.contains("energy") || lowerKey.contains("kcal") || lowerKey.contains("calories")) {
                    System.out.println("      Found direct calories: " + key + " = " + nutrients.get(key));
                    return nutrients.get(key);
                }
            } else if (lowerTarget.equals("fiber") || lowerTarget.equals("fibre")) {
                if (lowerKey.contains("fibre") || lowerKey.contains("fiber")) {
                    System.out.println("      Found fiber: " + key + " = " + nutrients.get(key));
                    return nutrients.get(key);
                }
            } else if (lowerTarget.equals("fat")) {
                // For fat, we need to sum all fat-related nutrients
                double totalFat = 0.0;
                for (String fatKey : nutrients.keySet()) {
                    String fatLowerKey = fatKey.toLowerCase();
                    if (fatLowerKey.contains("fat") || fatLowerKey.contains("lipid")) {
                        totalFat += nutrients.get(fatKey);
                        System.out.println("      Found fat: " + fatKey + " = " + nutrients.get(fatKey));
                    }
                }
                if (totalFat > 0) {
                    System.out.println("      Total fat: " + totalFat);
                    return totalFat;
                }
            } else if (lowerTarget.equals("protein")) {
                if (lowerKey.contains("protein")) {
                    return nutrients.get(key);
                }
            } else if (lowerTarget.equals("sugar")) {
                if (lowerKey.contains("sugar")) {
                    return nutrients.get(key);
                }
            } else if (lowerKey.contains(lowerTarget)) {
                return nutrients.get(key);
            }
        }
        
        // If calories not found directly, calculate from macronutrients
        if (targetNutrient.toLowerCase().equals("calories") || targetNutrient.toLowerCase().equals("energy")) {
            double calculatedCalories = calculateCaloriesFromMacros(nutrients);
            if (calculatedCalories > 0) {
                System.out.println("      Calculated calories from macros: " + calculatedCalories);
                return calculatedCalories;
            }
        }
        
        System.out.println("      No match found for: " + targetNutrient);
        return 0.0;
    }
    
    private double calculateCaloriesFromMacros(Map<String, Double> nutrients) {
        double carbs = 0, protein = 0, fat = 0;
        
        for (String key : nutrients.keySet()) {
            String lowerKey = key.toLowerCase();
            if (lowerKey.contains("carbohydrate") && !lowerKey.contains("fiber") && !lowerKey.contains("fibre")) {
                carbs += nutrients.get(key);
                System.out.println("      Found carbs: " + key + " = " + nutrients.get(key));
            } else if (lowerKey.contains("protein")) {
                protein += nutrients.get(key);
                System.out.println("      Found protein: " + key + " = " + nutrients.get(key));
            } else if (lowerKey.contains("fat") || lowerKey.contains("lipid")) {
                fat += nutrients.get(key);
                System.out.println("      Found fat: " + key + " = " + nutrients.get(key));
            }
        }
        double totalCalories = carbs * 4.0 + protein * 4.0 + fat * 9.0;
        System.out.println("      Calculated calories: carbs(" + carbs + "×4) + protein(" + protein + "×4) + fat(" + fat + "×9) = " + totalCalories);
        // Return as long as one macronutrient is greater than 0
        if (carbs > 0 || protein > 0 || fat > 0) {
            return totalCalories;
        }
        return 0.0;
    }
    
    // Generate reason for the swap - show all relevant nutrient changes
    private String generateReason(model.FoodItem current, model.FoodItem alternative, Goal goal, 
                                 double currentValue, double altValue) {
        StringBuilder reason = new StringBuilder();
        reason.append(String.format("Replace %s with %s to ", current.getName(), alternative.getName()));
        
        // Show the primary goal change
        String nutrient = goal.getNutrient();
        String direction = goal.getDirection();
        reason.append(String.format("%s %s (%.1f → %.1f)", 
            direction.toLowerCase(), nutrient, currentValue, altValue));
        
        // Check if this alternative also helps with other user goals
        if (userGoals != null && userGoals.size() > 1) {
            Map<String, Double> currentNutrients = current.getNutrients();
            Map<String, Double> altNutrients = alternative.getNutrients();
            
            List<String> additionalChanges = new ArrayList<>();
            
            for (Goal otherGoal : userGoals) {
                if (otherGoal.getNutrient().equalsIgnoreCase(nutrient)) {
                    continue; // Skip the primary goal
                }
                
                String otherNutrient = otherGoal.getNutrient();
                String otherDirection = otherGoal.getDirection();
                
                double currentOtherValue = getNutrientValue(currentNutrients, otherNutrient);
                double altOtherValue = getNutrientValue(altNutrients, otherNutrient);
                
                // Always show the change for other goals, regardless of whether it helps or not
                String changeDescription;
                if (Math.abs(altOtherValue - currentOtherValue) < 0.1) {
                    // No significant change
                    changeDescription = String.format("no change in %s (%.1f)", otherNutrient, currentOtherValue);
                } else {
                    // There is a change - determine actual direction based on values
                    String actualDirection;
                    if (altOtherValue > currentOtherValue) {
                        actualDirection = "increase";
                    } else {
                        actualDirection = "decrease";
                    }
                    changeDescription = String.format("%s %s (%.1f → %.1f)", 
                        actualDirection, otherNutrient, currentOtherValue, altOtherValue);
                }
                
                additionalChanges.add(changeDescription);
            }
            
            // Add additional changes to the reason
            if (!additionalChanges.isEmpty()) {
                reason.append(", ");
                reason.append(String.join(", ", additionalChanges));
            }
        }
        
        return reason.toString();
    }
    

    

    
    private void applyAllSuggestions(List<Meal> dailyMeals, List<SwapSuggestion> suggestions) {
        try {
            // Apply suggestions to each meal
            for (Meal meal : dailyMeals) {
                List<SwapSuggestion> mealSuggestions = suggestions.stream()
                    .filter(s -> s.getOriginal().getFoodID() == meal.getMealID())
                    .toList();
                
                if (!mealSuggestions.isEmpty()) {
                    Meal modifiedMeal = swapService.applySwapsToMeal(meal.getMealID(), mealSuggestions);
                    if (modifiedMeal != null) {
                        System.out.println("Successfully applied swaps to meal " + meal.getMealID());
                    }
                }
            }
            
            // Refresh the meals list
            this.meals = controller.getMealsForUser(userId);
            if (this.meals == null) {
                this.meals = new ArrayList<>();
            }
            populateDailySummaryTable();
            
        } catch (Exception e) {
            System.err.println("Error applying suggestions: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error applying suggestions: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

class MealJournalApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Meal Journal");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 300);
            frame.setLocationRelativeTo(null);
            frame.add(new JournalPanel(1)); // Pass dummy userId
            frame.setVisible(true);
        });
    }
}
