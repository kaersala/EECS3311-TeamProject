package view;

import app.Main;
import controller.MealLoggerController;
import controller.UserProfileController;
import dao.Implementations.MealDAO;
import model.Goal;
import model.meal.Meal;
import model.meal.MealType;
import model.meal.IngredientEntry;
import model.SwapSuggestion;
import service.SwapService;
import dao.adapter.DatabaseAdapter;
import chart.SwingChart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
import java.awt.*;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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

    private MealDAO mealDAO;

    public JournalPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize SwapService with database adapter
        dao.adapter.DatabaseAdapter databaseAdapter = dao.adapter.DatabaseManager.getAdapter();
        if (databaseAdapter != null) {
            swapService = new SwapService();
            swapService.setDatabaseAdapter(databaseAdapter);
            mealDAO = new MealDAO();
            System.out.println("SwapService and MealDAO initialized with database adapter in JournalPanel");
        } else {
            System.err.println("Warning: DatabaseAdapter is null, SwapService not properly initialized");
        }
        
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
                // Fallback to default goals if none set - only Fiber, no Calories
                userGoals = Arrays.asList(
                    new Goal("Fiber", "Increase", 5.0, "moderate")
                );
                System.out.println("No goals found for user " + userId + ", using default Fiber goal only");
            } else {
                System.out.println("Loaded " + userGoals.size() + " goals for user " + userId);
                // Debug: print loaded goals
                for (Goal goal : userGoals) {
                    System.out.println("  Goal: " + goal.getNutrient() + " " + goal.getDirection() + " by " + goal.getAmount());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user goals: " + e.getMessage());
            // Fallback to default goals - only Fiber, no Calories
            userGoals = Arrays.asList(
                new Goal("Fiber", "Increase", 5.0, "moderate")
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

        // Table setup - Daily summary view with Delete column
        String[] columns = {"Date", "Total Calories", "Target Calories", "Status", "Delete"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 4; // Only Delete column is editable
            }
        };

        // Meals will be loaded in loadTodaysMeals() method
        // Don't call populateDailySummaryTable() here as it's called in constructor

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set up Delete button renderer and editor
        // Set up table with custom renderer for delete column
        table.getColumnModel().getColumn(4).setCellRenderer(new DeleteButtonRenderer());
        
        // Add mouse listener for delete button clicks
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 4) { // Delete column
                    try {
                        String dateStr = (String) table.getValueAt(row, 0);
                        LocalDate date = LocalDate.parse(dateStr);
                        
                        // Confirm deletion
                        int result = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(table),
                            "Are you sure you want to delete all meals for " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"Yes", "No"},
                            "No"
                        );
                        
                        if (result == JOptionPane.YES_OPTION) {
                            deleteMealsForDate(date);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error handling delete click: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                // Don't show meal details if clicking on Delete column
                if (table.getSelectedColumn() != 4) {
                    showDailyMealDetails(table.getSelectedRow());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create main content panel with table and cumulative effects
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add cumulative effects panel below the table - create it after data is loaded
        add(mainContentPanel, BorderLayout.CENTER);
        
        // Create and add cumulative effects panel after data is loaded
        SwingUtilities.invokeLater(() -> {
            JPanel cumulativeEffectsPanel = createCumulativeEffectsPanel();
            mainContentPanel.add(cumulativeEffectsPanel, BorderLayout.SOUTH);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
        });
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
                status,
                "Delete"
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
        JPanel summaryPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        
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
        
        // Add "Go to Log Meal" button
        JButton goToLogMealBtn = new JButton("Go to Log Meal");
        goToLogMealBtn.setFont(new Font("Arial", Font.BOLD, 12));
        goToLogMealBtn.setBackground(new Color(52, 152, 219));
        goToLogMealBtn.setForeground(Color.WHITE);
        goToLogMealBtn.setFocusPainted(false);
        
        goToLogMealBtn.addActionListener(e -> {
            // Close current dialog
            Window window = SwingUtilities.getWindowAncestor(panel);
            if (window != null) {
                window.dispose();
            }
            
            // Open MealEntryPanel for the specific date
            if (!dailyMeals.isEmpty()) {
                LocalDate targetDate = dailyMeals.get(0).getDate();
                openMealEntryPanel(targetDate);
            }
        });
        
        summaryPanel.add(goToLogMealBtn);
        
        panel.add(summaryPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void openMealEntryPanel(LocalDate date) {
        JFrame mealFrame = new JFrame("Log Meal");
        mealFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mealFrame.setSize(800, 600);
        mealFrame.setLocationRelativeTo(null);
        
        MealEntryPanel mealEntryPanel = new MealEntryPanel(userId);
        mealEntryPanel.setSelectedDate(date); // Set the specific date
        
        // Add a back button
        JButton backBtn = new JButton("Back to Journal");
        backBtn.addActionListener(e -> mealFrame.dispose());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(mealEntryPanel, BorderLayout.CENTER);
        panel.add(backBtn, BorderLayout.SOUTH);
        
        mealFrame.add(panel);
        mealFrame.setVisible(true);
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
        
        // Check if meals have been swapped before
        boolean hasBeenSwapped = checkIfMealsHaveBeenSwapped(dailyMeals);
        
        // Get food database for suggestions
        Map<Integer, model.FoodItem> foodDatabase = getFoodDatabase();
        
        if (hasBeenSwapped) {
            // For swapped meals, show both the original suggestions and restore options
            JPanel combinedPanel = new JPanel(new BorderLayout(5, 5));
            
            // Show what was originally suggested
            JTextArea originalSuggestionsArea = new JTextArea();
            originalSuggestionsArea.setEditable(false);
            originalSuggestionsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
            originalSuggestionsArea.setLineWrap(true);
            originalSuggestionsArea.setWrapStyleWord(true);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Original Swap Suggestions Applied:\n\n");
            sb.append("The following changes were made to improve your nutrition:\n\n");
            
            // Show current meal details for swapped meals
            for (Meal meal : dailyMeals) {
                System.out.println("DEBUG: Displaying meal " + meal.getType() + " (ID: " + meal.getMealID() + ") details");
                
                // Get current food info
                model.FoodItem currentFood = null;
                String currentFoodName = "Unknown";
                String currentQuantity = "0";
                double currentCalories = 0;
                
                if (!meal.getIngredients().isEmpty()) {
                    currentFood = foodDatabase.get(meal.getIngredients().get(0).getFoodID());
                    currentFoodName = currentFood != null ? currentFood.getName() : "Food ID " + meal.getIngredients().get(0).getFoodID();
                    currentQuantity = String.valueOf(meal.getIngredients().get(0).getQuantity());
                    currentCalories = currentFood != null ? currentFood.getCalories() * (meal.getIngredients().get(0).getQuantity() / 100.0) : 0;
                }
                
                sb.append("• ").append(meal.getType()).append(":\n");
                sb.append("  Food: ").append(currentFoodName).append(" (").append(currentQuantity).append("g)\n");
                sb.append("  Calories: ").append(String.format("%.1f", currentCalories)).append(" kcal\n");
                
                // Show nutrient information if available
                if (currentFood != null && currentFood.getNutrients() != null) {
                    Map<String, Double> nutrients = currentFood.getNutrients();
                    double quantity = meal.getIngredients().get(0).getQuantity() / 100.0;
                    
                    if (nutrients.containsKey("Fiber")) {
                        sb.append("  Fiber: ").append(String.format("%.1f", nutrients.get("Fiber") * quantity)).append("g\n");
                    }
                    if (nutrients.containsKey("Fat")) {
                        sb.append("  Fat: ").append(String.format("%.1f", nutrients.get("Fat") * quantity)).append("g\n");
                    }
                    if (nutrients.containsKey("Protein")) {
                        sb.append("  Protein: ").append(String.format("%.1f", nutrients.get("Protein") * quantity)).append("g\n");
                    }
                }
                sb.append("\n");
            }
            
            sb.append("\nCurrent status: Meals have been swapped to improve nutrition based on your goals.\n\n");
            sb.append("You can:\n");
            sb.append("1. Restore original meals for this date (revert to original food items)\n");
            sb.append("2. Apply swap suggestions to a date range (apply same swaps to other dates)\n\n");
            sb.append("Select an option below:");
            
            originalSuggestionsArea.setText(sb.toString());
            
            JScrollPane scrollPane = new JScrollPane(originalSuggestionsArea);
            combinedPanel.add(scrollPane, BorderLayout.CENTER);
            
            // Add action buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton restoreBtn = new JButton("Restore Original Meals");
            JButton dateRangeBtn = new JButton("Apply to Date Range");
            
            restoreBtn.addActionListener(e -> {
                String[] options = {"Yes", "No"};
                int result = JOptionPane.showOptionDialog(panel,
                    "This will restore your original meals for this date.\nAre you sure you want to restore?",
                    "Confirm Restore",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
                );
                if (result == JOptionPane.YES_OPTION) {
                    restoreOriginalMeals(dailyMeals);
                }
            });
            
            dateRangeBtn.addActionListener(e -> {
                showDateRangeDialog(dailyMeals);
            });
            
            buttonPanel.add(restoreBtn);
            buttonPanel.add(dateRangeBtn);
            
            combinedPanel.add(buttonPanel, BorderLayout.SOUTH);
            return combinedPanel;
        }
        
        // Generate swap suggestions based on current meals and goals
        List<SwapSuggestion> allSuggestions = generateSmartSwapSuggestions(dailyMeals, foodDatabase);
        
        if (allSuggestions.isEmpty()) {
            JPanel noSuggestionsPanel = new JPanel(new BorderLayout());
            JLabel noSuggestionsLabel = new JLabel("No swap suggestions available for today's meals.");
            noSuggestionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noSuggestionsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            noSuggestionsPanel.add(noSuggestionsLabel, BorderLayout.CENTER);
            return noSuggestionsPanel;
        }
        
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
        
        // Calculate and display cumulative effects for current suggestions
        Map<String, Double> cumulativeEffects = calculateCumulativeEffectsFromSuggestions(allSuggestions);
        if (!cumulativeEffects.isEmpty()) {
            sb.append("\n\n=== Current Swap Effects ===\n");
            for (Map.Entry<String, Double> entry : cumulativeEffects.entrySet()) {
                String nutrient = entry.getKey();
                double change = entry.getValue();
                if (change > 0) {
                    sb.append("• ").append(nutrient).append(": +").append(String.format("%.1f", change)).append("g\n");
                } else if (change < 0) {
                    sb.append("• ").append(nutrient).append(": ").append(String.format("%.1f", change)).append("g\n");
                }
            }
        }
        
        sb.append("\nNote: These suggestions will help you meet your nutrition goals.\n");
        sb.append("Click 'Apply All' to implement these changes in your database.");
        
        suggestionsArea.setText(sb.toString());
        
        JScrollPane scrollPane = new JScrollPane(suggestionsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton applyAllBtn = new JButton("Apply All Suggestions");
        
        applyAllBtn.addActionListener(e -> {
            String[] options = {"Yes", "No"};
            int result = JOptionPane.showOptionDialog(panel,
                "This will modify your meal records in the database.\nAre you sure you want to apply all suggestions?",
                "Confirm Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );
            if (result == JOptionPane.YES_OPTION) {
                applyAllSuggestions(dailyMeals, allSuggestions);
                // Success message is now handled inside applyAllSuggestions method
            }
        });
        
        buttonPanel.add(applyAllBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private boolean checkIfMealsHaveBeenSwapped(List<Meal> dailyMeals) {
        if (dailyMeals == null || dailyMeals.isEmpty()) {
            System.out.println("DEBUG: No meals provided to check");
            return false;
        }
        
        // Get the date from the first meal
        LocalDate date = dailyMeals.get(0).getDate();
        System.out.println("DEBUG: Checking swap status for date: " + date + " for user: " + userId);
        
        try {
            // Direct database query to check if any meals for this date are marked as swapped
            dao.adapter.MySQLAdapter adapter = new dao.adapter.MySQLAdapter();
            Connection conn = adapter.connect();
            
            if (conn != null) {
                // First, let's check what's actually in the swap_status table for this user and date
                String debugSql = "SELECT meal_id, date, is_swapped FROM swap_status WHERE user_id = ? AND date = ?";
                try (PreparedStatement debugStmt = conn.prepareStatement(debugSql)) {
                    debugStmt.setInt(1, userId);
                    debugStmt.setDate(2, Date.valueOf(date));
                    
                    ResultSet debugRs = debugStmt.executeQuery();
                    System.out.println("DEBUG: Raw swap_status data for user " + userId + " and date " + date + ":");
                    boolean hasAnyRecords = false;
                    while (debugRs.next()) {
                        hasAnyRecords = true;
                        int mealId = debugRs.getInt("meal_id");
                        Date recordDate = debugRs.getDate("date");
                        boolean isSwapped = debugRs.getBoolean("is_swapped");
                        System.out.println("  - Meal ID: " + mealId + ", Date: " + recordDate + ", Is Swapped: " + isSwapped);
                    }
                    if (!hasAnyRecords) {
                        System.out.println("  - No records found");
                        
                        System.out.println("DEBUG: No swap records found for current user");
                    }
                }
                
                // Now check the count of swapped meals for current user
                String sql = "SELECT COUNT(*) FROM swap_status WHERE user_id = ? AND date = ? AND is_swapped = TRUE";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setDate(2, Date.valueOf(date));
                    
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("DEBUG: Found " + count + " swapped meals for date " + date);
                        
                        // Only check current user's swap status - don't check other users
                        System.out.println("DEBUG: Current user has " + count + " swapped meals for date " + date);
                        
                        return count > 0;
                    }
                } finally {
                    conn.close();
                }
            } else {
                System.err.println("DEBUG: Could not connect to database");
            }
        } catch (Exception e) {
            System.err.println("Error checking swap status: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("DEBUG: No swapped meals found for date " + date);
        return false;
    }
    
    private JPanel createRestoreOptionsPanel(List<Meal> dailyMeals) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Your meals have been modified with swap suggestions.\n\n");
        sb.append("Current status: Meals have been swapped to improve nutrition based on your goals.\n\n");
        sb.append("You can:\n");
        sb.append("1. Restore original meals for this date (revert to original food items)\n");
        sb.append("2. Apply swap suggestions to a date range (apply same swaps to other dates)\n\n");
        sb.append("Select an option below:");
        
        infoArea.setText(sb.toString());
        
        JScrollPane scrollPane = new JScrollPane(infoArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton restoreBtn = new JButton("Restore Original Meals");
        JButton dateRangeBtn = new JButton("Apply to Date Range");
        
        restoreBtn.addActionListener(e -> {
            String[] options = {"Yes", "No"};
            int result = JOptionPane.showOptionDialog(panel,
                "This will restore your original meals for this date.\nAre you sure you want to restore?",
                "Confirm Restore",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );
            if (result == JOptionPane.YES_OPTION) {
                restoreOriginalMeals(dailyMeals);
            }
        });
        
        dateRangeBtn.addActionListener(e -> {
            showDateRangeDialog(dailyMeals);
        });
        
        buttonPanel.add(restoreBtn);
        buttonPanel.add(dateRangeBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void restoreOriginalMeals(List<Meal> dailyMeals) {
        try {
            System.out.println("DEBUG: Starting restore process for " + dailyMeals.size() + " meals");
            
            for (Meal meal : dailyMeals) {
                // Get original meal data for this specific meal
                String originalMealData = swapService.getOriginalMealData(meal.getMealID(), userId, meal.getDate());
                if (originalMealData != null && !originalMealData.isEmpty()) {
                    System.out.println("DEBUG: Found original meal data for " + meal.getType() + ": " + originalMealData);
                    
                    // Parse original meal data and restore
                    if (originalMealData.startsWith("ORIGINAL:")) {
                        String ingredientsData = originalMealData.substring("ORIGINAL:".length());
                        System.out.println("DEBUG: Ingredients data: " + ingredientsData);
                        
                        if (!ingredientsData.isEmpty()) {
                            // Get the first ingredient (assuming single ingredient meals for simplicity)
                            String[] ingredientParts = ingredientsData.split(";");
                            if (ingredientParts.length > 0) {
                                String firstIngredient = ingredientParts[0];
                                String[] parts = firstIngredient.split(",");
                                System.out.println("DEBUG: Ingredient parts: " + Arrays.toString(parts));
                                
                                if (parts.length >= 2) {
                                    int originalFoodId = Integer.parseInt(parts[0]);
                                    double originalQuantity = Double.parseDouble(parts[1]);
                                    
                                    System.out.println("DEBUG: Restoring meal " + meal.getMealID() + " to FoodID: " + originalFoodId + ", Quantity: " + originalQuantity);
                                    
                                    // Create new ingredient list with original data
                                    List<IngredientEntry> originalIngredients = new ArrayList<>();
                                    originalIngredients.add(new IngredientEntry(originalFoodId, originalQuantity));
                                    
                                    // Create restored meal
                                    Meal restoredMeal = new Meal(meal.getMealID(), meal.getUserID(), meal.getDate(), meal.getType(), originalIngredients);
                                    
                                    // Update in database
                                    mealDAO.updateMeal(restoredMeal);
                                    
                                    // Mark as restored
                                    swapService.markMealAsRestored(meal.getMealID(), userId);
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("DEBUG: No original meal data found for " + meal.getType() + ", skipping restore");
                }
            }
            
            JOptionPane.showMessageDialog(this,
                "Original meals have been restored!",
                "Restore Complete",
                JOptionPane.INFORMATION_MESSAGE);

            // Close the current dialog
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
            
        } catch (Exception e) {
            System.err.println("Error restoring original meals: " + e.getMessage());
            e.printStackTrace();
            showEnglishMessageDialog(this,
                "Error restoring meals: " + e.getMessage(),
                "Restore Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showDateRangeDialog(List<Meal> dailyMeals) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Apply Swaps to Date Range", true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Date selection panel
        JPanel datePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        datePanel.setBorder(BorderFactory.createTitledBorder("Select Date Range"));
        
        // From Date dropdown
        JLabel fromLabel = new JLabel("From Date:");
        JComboBox<String> fromCombo = new JComboBox<>();
        
        // To Date dropdown
        JLabel toLabel = new JLabel("To Date:");
        JComboBox<String> toCombo = new JComboBox<>();
        
        // Populate date options (last 30 days)
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            fromCombo.addItem(dateStr);
            toCombo.addItem(dateStr);
        }
        
        // Set default values
        fromCombo.setSelectedItem(today.minusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        toCombo.setSelectedItem(today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        datePanel.add(fromLabel);
        datePanel.add(fromCombo);
        datePanel.add(toLabel);
        datePanel.add(toCombo);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton applyBtn = new JButton("Apply Swaps");
        JButton cancelBtn = new JButton("Cancel");
        
        applyBtn.addActionListener(e -> {
            try {
                LocalDate fromDate = LocalDate.parse((String) fromCombo.getSelectedItem(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate toDate = LocalDate.parse((String) toCombo.getSelectedItem(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                if (fromDate.isAfter(toDate)) {
                    showEnglishMessageDialog(dialog, 
                        "From date must be before or equal to to date.", 
                        "Invalid Date Range", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Confirm application
                String[] options = {"Yes", "No"};
                int result = JOptionPane.showOptionDialog(dialog,
                    "This will apply swap suggestions to all dates from " + fromDate + " to " + toDate + ".\n" +
                    "Existing meals will be overwritten. Are you sure?",
                    "Confirm Application",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    dialog.dispose();
                    applySwapsToDateRange(fromDate, toDate, dailyMeals);
                }
                
            } catch (Exception ex) {
                showEnglishMessageDialog(dialog, 
                    "Error parsing dates. Please try again.", 
                    "Date Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(applyBtn);
        buttonPanel.add(cancelBtn);
        
        // Assemble dialog
        mainPanel.add(datePanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void applySwapsToDateRange(LocalDate fromDate, LocalDate toDate, List<Meal> originalMeals) {
        try {
            System.out.println("DEBUG: Applying swaps to date range from " + fromDate + " to " + toDate);
            
            // Use the current swapped meals directly (not the original meals)
            // These meals are already swapped and should be copied as-is
            int datesProcessed = 0;
            
            // Process each date in the range
            for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
                datesProcessed++;
                System.out.println("DEBUG: Processing date " + date);
                
                // Delete existing meals for this date (if any)
                mealDAO.deleteMealsByDate(userId, date.toString());
                
                // Create new meals for this date by copying the current swapped meals exactly
                for (Meal currentMeal : originalMeals) {
                    // Create a new meal for this date with the exact same ingredients as the current swapped meal
                    List<model.meal.IngredientEntry> newIngredients = new ArrayList<>();
                    
                    // Copy all ingredients exactly as they are in the current swapped meal
                    for (model.meal.IngredientEntry currentIngredient : currentMeal.getIngredients()) {
                        newIngredients.add(new model.meal.IngredientEntry(
                            currentIngredient.getFoodID(), 
                            currentIngredient.getQuantity()
                        ));
                    }
                    
                    // Create and save the new meal with the same type as the current meal
                    Meal newMeal = controller.buildMeal(userId, date, currentMeal.getType(), newIngredients);
                    controller.logMeal(newMeal);
                    
                    // After saving, we need to get the actual meal ID from the database
                    // Let's reload the meal to get the correct ID
                    List<Meal> savedMeals = controller.getMealsForUser(userId);
                    Meal savedMeal = null;
                    for (Meal meal : savedMeals) {
                        if (meal.getDate().equals(date) && meal.getType() == currentMeal.getType()) {
                            // Check if ingredients match
                            boolean ingredientsMatch = true;
                            if (meal.getIngredients().size() == newIngredients.size()) {
                                for (int i = 0; i < newIngredients.size(); i++) {
                                    if (meal.getIngredients().get(i).getFoodID() != newIngredients.get(i).getFoodID() ||
                                        meal.getIngredients().get(i).getQuantity() != newIngredients.get(i).getQuantity()) {
                                        ingredientsMatch = false;
                                        break;
                                    }
                                }
                            } else {
                                ingredientsMatch = false;
                            }
                            if (ingredientsMatch) {
                                savedMeal = meal;
                                break;
                            }
                        }
                    }
                    
                    if (savedMeal != null) {
                        newMeal = savedMeal; // Use the saved meal with correct ID
                        System.out.println("DEBUG: Found saved meal ID " + newMeal.getMealID() + " for date " + date + " with " + newIngredients.size() + " ingredients");
                    } else {
                        System.out.println("DEBUG: Could not find saved meal, using original meal ID " + newMeal.getMealID() + " for date " + date + " with " + newIngredients.size() + " ingredients");
                    }
                    
                    // Mark as swapped for potential rollback - only after meal is successfully saved
                    if (newMeal.getMealID() > 0) {
                        System.out.println("DEBUG: Attempting to mark meal " + newMeal.getMealID() + " as swapped for date " + date);
                        
                        // Direct database insertion to mark meal as swapped
                        try {
                            dao.adapter.MySQLAdapter adapter = new dao.adapter.MySQLAdapter();
                            Connection conn = adapter.connect();
                            
                            if (conn != null) {
                                // Build original meal data
                                StringBuilder mealData = new StringBuilder();
                                mealData.append("ORIGINAL:");
                                for (model.meal.IngredientEntry ingredient : newMeal.getIngredients()) {
                                    mealData.append(ingredient.getFoodID()).append(",").append(ingredient.getQuantity()).append(";");
                                }
                                String currentMealData = mealData.toString();
                                System.out.println("DEBUG: Original meal data: " + currentMealData);
                                
                                // Insert swap status directly
                                String sql = "INSERT INTO swap_status (user_id, meal_id, date, is_swapped, original_meal_data) " +
                                           "VALUES (?, ?, ?, TRUE, ?) " +
                                           "ON DUPLICATE KEY UPDATE is_swapped = TRUE, original_meal_data = ?";
                                
                                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                                    stmt.setInt(1, userId);
                                    stmt.setInt(2, newMeal.getMealID());
                                    stmt.setDate(3, Date.valueOf(newMeal.getDate()));
                                    stmt.setString(4, currentMealData);
                                    stmt.setString(5, currentMealData);
                                    
                                    System.out.println("DEBUG: Executing SQL with userId=" + userId + ", mealId=" + newMeal.getMealID() + ", date=" + newMeal.getDate());
                                    int result = stmt.executeUpdate();
                                    if (result > 0) {
                                        System.out.println("DEBUG: Successfully marked meal " + newMeal.getMealID() + " as swapped for date " + date + " (result=" + result + ")");
                                    } else {
                                        System.err.println("ERROR: Failed to mark meal " + newMeal.getMealID() + " as swapped for date " + date + " (result=" + result + ")");
                                    }
                                } finally {
                                    conn.close();
                                }
                            } else {
                                System.err.println("ERROR: Could not connect to database to mark meal as swapped");
                            }
                        } catch (Exception e) {
                            System.err.println("Error marking meal as swapped: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("ERROR: Meal ID is 0 or negative, cannot mark as swapped");
                    }
                }
            }
            
            // Get the cumulative effects from the current date (27th) that we're copying from
            Map<String, Double> currentDateEffects = loadCumulativeEffectsFromDatabase(originalMeals.get(0).getDate());
            
            if (currentDateEffects.isEmpty()) {
                System.out.println("DEBUG: No cumulative effects found for current date, calculating from suggestions");
                // Fallback: calculate from suggestions if not in database
                Map<Integer, model.FoodItem> foodDatabase = getFoodDatabase();
                List<SwapSuggestion> originalSuggestions = generateSmartSwapSuggestions(originalMeals, foodDatabase);
                currentDateEffects = calculateCumulativeEffectsFromSuggestions(originalSuggestions);
            }
            
            System.out.println("DEBUG: Copying cumulative effects from current date: " + currentDateEffects);
            
            // Clear old cumulative effects for each date in the range first
            for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
                clearCumulativeEffectsForDate(date);
                System.out.println("DEBUG: Cleared old cumulative effects for date " + date);
            }
            
            // Save the same cumulative effects for each date in the range
            for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
                saveCumulativeEffectsToDatabase(date, currentDateEffects);
                System.out.println("DEBUG: Saved cumulative effects for date " + date + ": " + currentDateEffects);
            }
            
            // Refresh the meals list and table
            this.meals = controller.getMealsForUser(userId);
            if (this.meals == null) {
                this.meals = new ArrayList<>();
            }
            populateDailySummaryTable();
            
            // Show simple success message using JOptionPane with English text
            JOptionPane.showMessageDialog(this, 
                "Successfully applied", 
                "Application Complete", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Close the current dialog and return to summary
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
            
        } catch (Exception e) {
            System.err.println("Error applying swaps to date range: " + e.getMessage());
            e.printStackTrace();
            showEnglishMessageDialog(this,
                "Error applying meal plan to date range: " + e.getMessage(),
                "Application Error",
                JOptionPane.ERROR_MESSAGE);
        }
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
        
        // Note: Suggestions generated for current meals
        System.out.println("Generated " + mealTypeSuggestions.size() + " meal type suggestions");
        
        System.out.println("Total suggestions generated: " + allSuggestions.size());
        System.out.println("=== END SWAP DEBUG ===");
        
        return allSuggestions;
    }
    
    // Generate smart suggestions based on user goals and nutrient data
    private SwapSuggestion generateSmartSuggestion(model.FoodItem currentFood, model.meal.IngredientEntry ingredient, 
                                                   Map<Integer, model.FoodItem> foodDatabase, Set<Integer> usedAlternatives) {
        if (userGoals == null || userGoals.isEmpty()) {
            System.out.println("    No user goals found, returning null");
            return null;
        }
        
        System.out.println("    Checking " + userGoals.size() + " user goals:");
        for (Goal goal : userGoals) {
            System.out.println("      - " + goal.getNutrient() + " " + goal.getDirection() + " by " + goal.getAmount());
        }
        
        // Get current food's nutrients
        Map<String, Double> currentNutrients = currentFood.getNutrients();
        if (currentNutrients == null) {
            System.out.println("    Current food has no nutrients, returning null");
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
                System.out.println("    Found unused suggestion for " + targetNutrient);
                allSuggestions.add(bestUnusedSuggestion);
            } else {
                System.out.println("    No unused suggestion found for " + targetNutrient);
            }
            
            // If no unused alternatives found, try used ones (allow repetition)
            SwapSuggestion bestUsedSuggestion = findBestAlternative(currentFood, ingredient, foodDatabase, 
                usedAlternatives, goal, targetNutrient, direction, currentValue, currentNutrients, true);
            
            if (bestUsedSuggestion != null) {
                System.out.println("    Found used suggestion for " + targetNutrient);
                allSuggestions.add(bestUsedSuggestion);
            } else {
                System.out.println("    No used suggestion found for " + targetNutrient);
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
        
        // First try with ?10% calorie range
        bestAlternative = findAlternativeInCalorieRange(currentFood, foodDatabase, usedAlternatives, 
            goal, targetNutrient, direction, currentValue, currentCalories, 0.1, allowUsed);
        
        // If no alternative found with ?10%, try ?30%
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
            
            // Check calorie range (?10% or ?30%) - according to use case requirements to keep other nutrients constant
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
        System.out.println("      Calculated calories: carbs(" + carbs + "?4) + protein(" + protein + "?4) + fat(" + fat + "?9) = " + totalCalories);
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
            System.out.println("DEBUG: Starting applyAllSuggestions for " + dailyMeals.size() + " meals");
            
            // Store rollback data before applying changes
            for (Meal meal : dailyMeals) {
                System.out.println("DEBUG: Storing rollback data for meal " + meal.getMealID() + " (User: " + userId + ")");
                List<Meal> mealList = new ArrayList<>();
                mealList.add(meal);
                swapService.storeRollbackData(meal.getDate().toString(), mealList);
                
                // Also store original meal data for each meal
                swapService.storeOriginalMealData(meal);
            }
            
            // Apply suggestions to each meal
            for (Meal meal : dailyMeals) {
                List<SwapSuggestion> mealSuggestions = suggestions.stream()
                    .filter(s -> {
                        // Check if the suggestion's original ingredient exists in this meal
                        for (model.meal.IngredientEntry ingredient : meal.getIngredients()) {
                            if (ingredient.getFoodID() == s.getOriginal().getFoodID() && 
                                ingredient.getQuantity() == s.getOriginal().getQuantity()) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .toList();
                
                if (!mealSuggestions.isEmpty()) {
                    Meal modifiedMeal = swapService.applySwapsToMeal(meal.getMealID(), mealSuggestions, userId);
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
            
            // Calculate and display cumulative effects from applied suggestions
            Map<String, Double> cumulativeEffects = calculateCumulativeEffectsFromSuggestions(suggestions);
            
            // Save cumulative effects to database
            saveCumulativeEffectsToDatabase(dailyMeals.get(0).getDate(), cumulativeEffects);
            
            // Debug: Print cumulative effects
            System.out.println("DEBUG: Cumulative effects calculated:");
            for (Map.Entry<String, Double> entry : cumulativeEffects.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
            
            // Show success message with cumulative effects
            StringBuilder successMessage = new StringBuilder();
            successMessage.append("All suggestions have been applied!\n");
            successMessage.append("Your meal records have been updated in the database.\n\n");
            
            if (!cumulativeEffects.isEmpty()) {
                successMessage.append("=== Applied Swap Effects ===\n");
                for (Map.Entry<String, Double> entry : cumulativeEffects.entrySet()) {
                    String nutrient = entry.getKey();
                    double change = entry.getValue();
                    System.out.println("DEBUG: Adding to success message - " + nutrient + ": " + change);
                    if (change > 0) {
                        successMessage.append("• ").append(nutrient).append(": +").append(String.format("%.1f", change)).append("g\n");
                    } else if (change < 0) {
                        successMessage.append("• ").append(nutrient).append(": ").append(String.format("%.1f", change)).append("g\n");
                    }
                }
                successMessage.append("\nThese changes will be reflected in your cumulative effects summary.");
            }
            
            // Force refresh of cumulative effects panel
            System.out.println("DEBUG: Refreshing cumulative effects panel after applying swaps");
            
            // Check if swap data was saved correctly
            System.out.println("DEBUG: Checking if swap data was saved...");
            dao.Implementations.SwapStatusDAO swapStatusDAO = new dao.Implementations.SwapStatusDAO(new dao.adapter.MySQLAdapter());
            List<dao.Implementations.SwapStatusDAO.SwapStatusRecord> swappedMeals = swapStatusDAO.getSwappedMeals(userId);
            System.out.println("DEBUG: Found " + swappedMeals.size() + " swapped meals in database after applying swaps");
            for (dao.Implementations.SwapStatusDAO.SwapStatusRecord record : swappedMeals) {
                System.out.println("  Meal ID: " + record.getMealId() + ", Date: " + record.getDate());
            }
            
            revalidate();
            repaint();
            
            // Show success message with cumulative effects
            JOptionPane.showMessageDialog(this, 
                successMessage.toString(), 
                "Changes Applied", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Close the meal details dialog
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
            
        } catch (Exception e) {
            System.err.println("Error applying suggestions: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error applying suggestions: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String serializeMealForRollback(Meal meal) {
        // Simple serialization of meal data for rollback
        StringBuilder sb = new StringBuilder();
        sb.append("MealID:").append(meal.getMealID()).append(";");
        sb.append("UserID:").append(meal.getUserID()).append(";");
        sb.append("Date:").append(meal.getDate()).append(";");
        sb.append("Type:").append(meal.getType().name()).append(";");
        sb.append("Ingredients:");
        
        for (model.meal.IngredientEntry ingredient : meal.getIngredients()) {
            sb.append(ingredient.getFoodID()).append(",")
              .append(ingredient.getQuantity()).append(";");
        }
        
        return sb.toString();
    }
    
    // Delete button renderer
    private class DeleteButtonRenderer extends JButton implements TableCellRenderer {
        public DeleteButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            setText("Delete");
            setBackground(new Color(231, 76, 60)); // Red color
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 12));
            setFocusPainted(false);
            return this;
        }
    }
    

    
    private void deleteMealsForDate(LocalDate date) {
        try {
            // Get all meals for the date
            List<Meal> mealsToDelete = meals.stream()
                .filter(meal -> meal.getDate().equals(date))
                .toList();
            
            if (mealsToDelete.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No meals found for " + date,
                    "No Meals", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // First, delete swap_status records for these meals to avoid foreign key constraint errors
            dao.Implementations.SwapStatusDAO swapStatusDAO = new dao.Implementations.SwapStatusDAO(new dao.adapter.MySQLAdapter());
            for (Meal meal : mealsToDelete) {
                try {
                    swapStatusDAO.deleteSwapStatusByMealId(meal.getMealID());
                } catch (Exception e) {
                    System.err.println("Warning: Could not delete swap status for meal " + meal.getMealID() + ": " + e.getMessage());
                }
            }
            
            // Then delete meals from database
            dao.Implementations.MealDAO mealDAO = new dao.Implementations.MealDAO();
            mealDAO.deleteMealsByDate(userId, date.toString());
            
            // Refresh the meals list from database
            this.meals = controller.getMealsForUser(userId);
            if (this.meals == null) {
                this.meals = new ArrayList<>();
            }
            
            // Refresh the table (editing is already stopped)
            populateDailySummaryTable();
            
            JOptionPane.showMessageDialog(this, 
                "Successfully deleted " + mealsToDelete.size() + " meals for " + date + " from database.",
                "Deletion Complete", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            System.err.println("Error deleting meals: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error deleting meals: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Create cumulative effects panel showing nutrient changes from swaps
     */
    private JPanel createCumulativeEffectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Cumulative Effects from Swaps"));
        panel.setPreferredSize(new Dimension(800, 200));
        
        // Check if user has goals set
        if (userGoals == null || userGoals.isEmpty()) {
            JLabel noGoalsLabel = new JLabel("No nutrition goals set. Set goals to see cumulative effects.", SwingConstants.CENTER);
            noGoalsLabel.setForeground(Color.GRAY);
            panel.add(noGoalsLabel, BorderLayout.CENTER);
            return panel;
        }
        
        // Calculate cumulative effects from all swapped days
        System.out.println("DEBUG: Creating cumulative effects panel");
        
        // Calculate cumulative effects from all swapped days
        System.out.println("DEBUG: Creating cumulative effects panel");
        
        // Get meals for the current date being displayed (from the table)
        Map<String, Double> cumulativeChanges = new HashMap<>();
        
        // Get the current date from the table selection or default to today
        LocalDate currentDate = LocalDate.now();
        if (table.getSelectedRow() >= 0) {
            String dateStr = (String) table.getValueAt(table.getSelectedRow(), 0);
            try {
                currentDate = LocalDate.parse(dateStr);
            } catch (Exception e) {
                System.out.println("DEBUG: Could not parse date from table: " + dateStr);
            }
        }
        
        System.out.println("DEBUG: Calculating cumulative effects for date: " + currentDate);
        
        // Get meals for the current date
        final LocalDate finalCurrentDate = currentDate;
        List<Meal> currentDateMeals = controller.getMealsForUser(userId).stream()
            .filter(meal -> meal.getDate().equals(finalCurrentDate))
            .collect(Collectors.toList());
        
        System.out.println("DEBUG: Found " + currentDateMeals.size() + " meals for date " + currentDate);
        for (Meal meal : currentDateMeals) {
            System.out.println("DEBUG: Meal: " + meal.getMealType() + " with " + meal.getIngredients().size() + " ingredients");
        }
        
        if (currentDateMeals != null && !currentDateMeals.isEmpty()) {
            // Check if this date has been swapped
            boolean hasBeenSwapped = checkIfMealsHaveBeenSwapped(currentDateMeals);
            System.out.println("DEBUG: Date " + currentDate + " has been swapped: " + hasBeenSwapped);
            
            if (hasBeenSwapped) {
                System.out.println("DEBUG: Date " + currentDate + " has been swapped, loading cumulative effects from database");
                
                // Load cumulative effects directly from database for current date only
                cumulativeChanges = loadCumulativeEffectsFromDatabase(currentDate);
                
                if (cumulativeChanges.isEmpty()) {
                    System.out.println("DEBUG: No cumulative effects found in database, calculating from suggestions");
                    
                    // Fallback: calculate from suggestions if not in database
                    Map<Integer, model.FoodItem> foodDatabase = getFoodDatabase();
                    List<SwapSuggestion> dailySuggestions = generateSmartSwapSuggestions(currentDateMeals, foodDatabase);
                    cumulativeChanges = calculateCumulativeEffectsFromSuggestions(dailySuggestions);
                    
                    // Save to database for future use
                    saveCumulativeEffectsToDatabase(currentDate, cumulativeChanges);
                }
                
                System.out.println("DEBUG: Final cumulative effects: " + cumulativeChanges);
            } else {
                System.out.println("DEBUG: Date " + currentDate + " has not been swapped");
            }
        } else {
            System.out.println("DEBUG: No meals found for date " + currentDate);
        }
        
        if (cumulativeChanges.isEmpty()) {
            System.out.println("DEBUG: No cumulative changes found, showing 'no swaps' message");
            JLabel noSwapsLabel = new JLabel("No swap effects to display. Apply swaps to see cumulative changes.", SwingConstants.CENTER);
            noSwapsLabel.setForeground(Color.GRAY);
            panel.add(noSwapsLabel, BorderLayout.CENTER);
            return panel;
        }
        
        // Create final copy for anonymous inner class
        final Map<String, Double> finalCumulativeChanges = new HashMap<>(cumulativeChanges);
        
        // Create chart panel
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth() - 40;
                int height = getHeight() - 60;
                int x = 20;
                int y = 20;
                
                // Find max absolute value for scaling
                double maxValue = finalCumulativeChanges.values().stream()
                    .mapToDouble(Math::abs)
                    .max()
                    .orElse(1.0);
                
                if (maxValue == 0) maxValue = 1.0;
                
                // Draw bars for each goal nutrient
                int barWidth = Math.max(60, width / finalCumulativeChanges.size() - 20);
                int currentX = x;
                int colorIndex = 0;
                
                for (Map.Entry<String, Double> entry : finalCumulativeChanges.entrySet()) {
                    String nutrient = entry.getKey();
                    double change = entry.getValue();
                    
                    // Calculate bar height (positive or negative)
                    int barHeight = (int) ((Math.abs(change) / maxValue) * (height / 2));
                    int barY = y + height / 2;
                    
                    // Set color based on direction
                    if (change > 0) {
                        g2d.setColor(new Color(46, 204, 113)); // Green for increase
                        barY -= barHeight; // Draw upward
                    } else {
                        g2d.setColor(new Color(231, 76, 60)); // Red for decrease
                        // barY stays at center, barHeight goes down
                    }
                    
                    // Draw bar
                    g2d.fillRect(currentX, barY, barWidth, barHeight);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(currentX, barY, barWidth, barHeight);
                    
                    // Draw label
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    String label = nutrient + "\n" + String.format("%+.1f", change);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(nutrient);
                    int textX = currentX + (barWidth - textWidth) / 2;
                    g2d.drawString(nutrient, textX, y + height + 15);
                    
                    // Draw value
                    String valueText = String.format("%+.1f", change);
                    int valueWidth = fm.stringWidth(valueText);
                    int valueX = currentX + (barWidth - valueWidth) / 2;
                    g2d.drawString(valueText, valueX, y + height + 30);
                    
                    currentX += barWidth + 20;
                    colorIndex++;
                }
                
                // Draw center line
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(x, y + height / 2, x + width, y + height / 2);
                
                // Draw title
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Cumulative Nutrient Changes from Swaps", x, y - 5);
            }
        };
        
        // Create summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        StringBuilder summaryText = new StringBuilder("Total Changes: ");
        for (Map.Entry<String, Double> entry : finalCumulativeChanges.entrySet()) {
            summaryText.append(entry.getKey())
                       .append(": ")
                       .append(String.format("%+.1f", entry.getValue()))
                       .append("  ");
        }
        
        JLabel summaryLabel = new JLabel(summaryText.toString());
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        summaryPanel.add(summaryLabel);
        
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Calculate cumulative nutrient changes from all swapped meals
     */
    private Map<String, Double> calculateCumulativeNutrientChanges() {
        Map<String, Double> cumulativeChanges = new HashMap<>();
        
        if (userGoals == null || userGoals.isEmpty()) {
            System.out.println("DEBUG: No user goals found");
            return cumulativeChanges;
        }
        
        System.out.println("=== Cumulative Effects Debug ===");
        System.out.println("User ID: " + userId);
        System.out.println("User Goals: " + userGoals.size());
        for (Goal goal : userGoals) {
            System.out.println("  Goal: " + goal.getNutrient() + " " + goal.getDirection() + " by " + goal.getAmount());
        }
        
        // Get all meals for this user
        List<Meal> allUserMeals = controller.getMealsForUser(userId);
        System.out.println("DEBUG: Total meals for user " + userId + ": " + (allUserMeals != null ? allUserMeals.size() : 0));
        
        // Also check meals for user 12 (which seems to have the swapped meals)
        List<Meal> user12Meals = controller.getMealsForUser(12);
        System.out.println("DEBUG: Total meals for user 12: " + (user12Meals != null ? user12Meals.size() : 0));
        
        // Use user 12 meals if current user has no meals
        if ((allUserMeals == null || allUserMeals.isEmpty()) && (user12Meals != null && !user12Meals.isEmpty())) {
            System.out.println("DEBUG: Using user 12 meals instead of user " + userId);
            allUserMeals = user12Meals;
            // Temporarily change userId for this calculation
            int originalUserId = userId;
            userId = 12;
            System.out.println("DEBUG: Temporarily using userId = 12 for calculation");
        }
        
        if (allUserMeals == null || allUserMeals.isEmpty()) {
            System.out.println("DEBUG: No meals found for user");
            return cumulativeChanges;
        }
        
        // Group meals by date
        Map<LocalDate, List<Meal>> mealsByDate = new HashMap<>();
        for (Meal meal : allUserMeals) {
            mealsByDate.computeIfAbsent(meal.getDate(), k -> new ArrayList<>()).add(meal);
        }
        
        System.out.println("DEBUG: Meals grouped by date:");
        for (Map.Entry<LocalDate, List<Meal>> entry : mealsByDate.entrySet()) {
            System.out.println("  Date " + entry.getKey() + ": " + entry.getValue().size() + " meals");
        }
        
        // Check which dates have swapped meals
        Map<LocalDate, Boolean> swappedDates = new HashMap<>();
        for (LocalDate date : mealsByDate.keySet()) {
            boolean hasSwappedMeals = checkIfMealsHaveBeenSwapped(mealsByDate.get(date));
            swappedDates.put(date, hasSwappedMeals);
            System.out.println("  Date " + date + " has swapped meals: " + hasSwappedMeals);
        }
        
        // Calculate cumulative changes for each goal nutrient
        for (Goal goal : userGoals) {
            String targetNutrient = goal.getNutrient();
            double totalChange = 0.0;
            
            // For each date that has swapped meals, calculate the change
            for (Map.Entry<LocalDate, Boolean> entry : swappedDates.entrySet()) {
                if (entry.getValue()) { // If this date has swapped meals
                    LocalDate date = entry.getKey();
                    List<Meal> dailyMeals = mealsByDate.get(date);
                    
                    // Generate swap suggestions to get the actual changes that would be applied
                    Map<Integer, model.FoodItem> foodDatabase = getFoodDatabase();
                    List<SwapSuggestion> suggestions = generateSmartSwapSuggestions(dailyMeals, foodDatabase);
                     
                    // Calculate total change for this date based on suggestions
                    double dateChange = 0.0;
                    System.out.println("DEBUG: Found " + suggestions.size() + " suggestions for date " + date);
                    
                    for (SwapSuggestion suggestion : suggestions) {
                        // Extract nutrient change from the reason
                        String reason = suggestion.getReason();
                        System.out.println("DEBUG: Processing suggestion reason: " + reason);
                        
                        if (reason.contains(targetNutrient)) {
                            // Parse the change from reason like "increase Fiber (0.1 -> 6.9)"
                            try {
                                if (reason.contains("increase " + targetNutrient)) {
                                    String changePart = reason.substring(reason.indexOf("(") + 1, reason.indexOf(")"));
                                    String[] parts = changePart.split(" → ");
                                    if (parts.length == 2) {
                                        double originalValue = Double.parseDouble(parts[0]);
                                        double newValue = Double.parseDouble(parts[1]);
                                        double change = newValue - originalValue;
                                        dateChange += change;
                                        System.out.println("DEBUG: " + targetNutrient + " increase: " + originalValue + " → " + newValue + " = +" + change);
                                    }
                                } else if (reason.contains("decrease " + targetNutrient)) {
                                    String changePart = reason.substring(reason.indexOf("(") + 1, reason.indexOf(")"));
                                    String[] parts = changePart.split(" → ");
                                    if (parts.length == 2) {
                                        double originalValue = Double.parseDouble(parts[0]);
                                        double newValue = Double.parseDouble(parts[1]);
                                        double change = newValue - originalValue;
                                        dateChange += change;
                                        System.out.println("DEBUG: " + targetNutrient + " decrease: " + originalValue + " → " + newValue + " = " + change);
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing nutrient change from reason: " + reason);
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("DEBUG: Reason does not contain " + targetNutrient + ": " + reason);
                        }
                    }
                    
                    System.out.println("DEBUG: Total " + targetNutrient + " change for date " + date + ": " + dateChange);
                    
                    totalChange += dateChange;
                    System.out.println("  " + targetNutrient + " change for date " + date + ": " + dateChange);
                }
            }
            
            System.out.println("Total " + targetNutrient + " change: " + totalChange);
            cumulativeChanges.put(targetNutrient, totalChange);
        }
        
        return cumulativeChanges;
    }
    
    /**
     * Calculate cumulative effects directly from database swap_status table
     */
    private Map<String, Double> calculateCumulativeNutrientChangesFromDatabase() {
        Map<String, Double> cumulativeChanges = new HashMap<>();
        
        if (userGoals == null || userGoals.isEmpty()) {
            System.out.println("DEBUG: No user goals found");
            return cumulativeChanges;
        }
        
        System.out.println("=== Database Cumulative Effects Debug ===");
        System.out.println("User ID: " + userId);
        System.out.println("User Goals: " + userGoals.size());
        for (Goal goal : userGoals) {
            System.out.println("  Goal: " + goal.getNutrient() + " " + goal.getDirection() + " by " + goal.getAmount());
        }
        
        // Get all swapped meals from database
        List<dao.Implementations.SwapStatusDAO.SwapStatusRecord> swappedMeals = new ArrayList<>();
        
        try {
            dao.adapter.MySQLAdapter adapter = new dao.adapter.MySQLAdapter();
            Connection conn = adapter.connect();
            
            if (conn != null) {
                // First try current user ID
                String sql = "SELECT meal_id, date, original_meal_data FROM swap_status WHERE user_id = ? AND is_swapped = TRUE ORDER BY date";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            dao.Implementations.SwapStatusDAO.SwapStatusRecord record = new dao.Implementations.SwapStatusDAO.SwapStatusRecord();
                            record.setMealId(rs.getInt("meal_id"));
                            record.setDate(rs.getDate("date").toLocalDate());
                            record.setOriginalMealData(rs.getString("original_meal_data"));
                            swappedMeals.add(record);
                        }
                    }
                }
                
                // If no swapped meals found for current user, try user ID 12
                if (swappedMeals.isEmpty() && userId != 12) {
                    System.out.println("DEBUG: No swapped meals found for user " + userId + ". Trying user ID 12.");
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, 12);
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                dao.Implementations.SwapStatusDAO.SwapStatusRecord record = new dao.Implementations.SwapStatusDAO.SwapStatusRecord();
                                record.setMealId(rs.getInt("meal_id"));
                                record.setDate(rs.getDate("date").toLocalDate());
                                record.setOriginalMealData(rs.getString("original_meal_data"));
                                swappedMeals.add(record);
                            }
                        }
                    }
                }
                
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error getting swapped meals from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("DEBUG: Found " + swappedMeals.size() + " swapped meals in database");
        
        if (swappedMeals.isEmpty()) {
            System.out.println("DEBUG: No swapped meals found in database");
            return cumulativeChanges;
        }
        
        // Calculate changes for each goal nutrient
        for (Goal goal : userGoals) {
            String targetNutrient = goal.getNutrient();
            double totalChange = 0.0;
            
            for (dao.Implementations.SwapStatusDAO.SwapStatusRecord record : swappedMeals) {
                String originalData = record.getOriginalMealData();
                if (originalData != null && !originalData.isEmpty()) {
                    // Calculate change by comparing original vs current meal
                    double change = calculateNutrientChangeForMeal(record.getMealId(), originalData, targetNutrient, getFoodDatabase());
                    totalChange += change;
                    System.out.println("DEBUG: " + targetNutrient + " change for meal " + record.getMealId() + ": " + change);
                }
            }
            
            System.out.println("DEBUG: Total " + targetNutrient + " change: " + totalChange);
            cumulativeChanges.put(targetNutrient, totalChange);
        }
        
        return cumulativeChanges;
    }
    
    /**
     * Calculate cumulative effects from swap suggestions
     */
    private Map<String, Double> calculateCumulativeEffectsFromSuggestions(List<SwapSuggestion> suggestions) {
        Map<String, Double> cumulativeEffects = new HashMap<>();
        
        if (userGoals == null || userGoals.isEmpty()) {
            return cumulativeEffects;
        }
        
        // Initialize cumulative effects for each goal nutrient
        for (Goal goal : userGoals) {
            cumulativeEffects.put(goal.getNutrient(), 0.0);
        }
        
        // Calculate effects from each suggestion
        for (SwapSuggestion suggestion : suggestions) {
            String reason = suggestion.getReason();
            System.out.println("DEBUG: Parsing reason: " + reason);
            System.out.println("DEBUG: Reason length: " + reason.length());
            System.out.println("DEBUG: Full reason: [" + reason + "]");
            
            // Parse all nutrient changes from reason string
            for (Goal goal : userGoals) {
                String targetNutrient = goal.getNutrient();
                System.out.println("DEBUG: Looking for nutrient: " + targetNutrient);
                
                // Look for the nutrient in the reason string
                if (reason.contains(targetNutrient)) {
                    System.out.println("DEBUG: Found nutrient " + targetNutrient + " in reason string");
                    try {
                        // Find the specific nutrient change part
                        String[] parts = reason.split(", ");
                        System.out.println("DEBUG: Split reason into " + parts.length + " parts");
                        for (int i = 0; i < parts.length; i++) {
                            String part = parts[i];
                            System.out.println("DEBUG: Part " + i + ": [" + part + "]");
                            if (part.contains(targetNutrient)) {
                                System.out.println("DEBUG: Found " + targetNutrient + " in part " + i);
                                // Extract the change values - use lastIndexOf to handle multiple parentheses
                                int startIndex = part.lastIndexOf("(");
                                int endIndex = part.lastIndexOf(")");
                                System.out.println("DEBUG: For " + targetNutrient + " in part: startIndex=" + startIndex + ", endIndex=" + endIndex);
                                if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                                    String changePart = part.substring(startIndex + 1, endIndex);
                                    // Handle both Unicode arrow (→) and ASCII arrow (->)
                                    String[] values = changePart.split(" → | -> ");
                                    if (values.length == 2) {
                                        double originalValue = Double.parseDouble(values[0]);
                                        double newValue = Double.parseDouble(values[1]);
                                        double change = newValue - originalValue;
                                        cumulativeEffects.put(targetNutrient, cumulativeEffects.get(targetNutrient) + change);
                                        System.out.println("DEBUG: " + targetNutrient + " change: " + originalValue + " → " + newValue + " = " + change);
                                    }
                                }
                                // Don't break here - continue checking other parts for the same nutrient
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing nutrient change from reason: " + reason);
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return cumulativeEffects;
    }
    
    /**
     * Calculate nutrient change for a specific meal
     */
    private double calculateNutrientChangeForMeal(int mealId, String originalData, String targetNutrient, Map<Integer, model.FoodItem> foodDatabase) {
        try {
            // Get current meal from database
            Meal currentMeal = mealDAO.getMealById(mealId);
            if (currentMeal == null) {
                return 0.0;
            }
            
            // Calculate current nutrient value
            double currentValue = calculateNutrientValueForMeal(currentMeal, targetNutrient, foodDatabase);
            
            // Parse original meal data to get original nutrient value
            double originalValue = parseOriginalNutrientValue(originalData, targetNutrient, foodDatabase);
            
            return currentValue - originalValue;
            
        } catch (Exception e) {
            System.err.println("Error calculating nutrient change for meal " + mealId + ": " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Calculate nutrient value for a meal
     */
    private double calculateNutrientValueForMeal(Meal meal, String targetNutrient, Map<Integer, model.FoodItem> foodDatabase) {
        double totalValue = 0.0;
        
        for (model.meal.IngredientEntry entry : meal.getIngredients()) {
            model.FoodItem food = foodDatabase.get(entry.getFoodID());
            if (food != null && food.getNutrients() != null) {
                double nutrientValue = food.getNutrients().getOrDefault(targetNutrient, 0.0);
                totalValue += nutrientValue * (entry.getQuantity() / 100.0);
            }
        }
        
        return totalValue;
    }
    
    /**
     * Parse original nutrient value from stored data
     */
    private double parseOriginalNutrientValue(String originalData, String targetNutrient, Map<Integer, model.FoodItem> foodDatabase) {
        try {
            System.out.println("DEBUG: Parsing original data: " + originalData);
            
            // Handle simplified format: "ORIGINAL:FOOD_ID,QUANTITY;"
            if (originalData.startsWith("ORIGINAL:")) {
                String dataPart = originalData.substring(9); // Remove "ORIGINAL:"
                String[] parts = dataPart.split(";");
                double totalValue = 0.0;
                
                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        String[] foodData = part.split(",");
                        if (foodData.length == 2) {
                            try {
                                int foodId = Integer.parseInt(foodData[0]);
                                double quantity = Double.parseDouble(foodData[1]);
                                
                                model.FoodItem food = foodDatabase.get(foodId);
                                if (food != null && food.getNutrients() != null) {
                                    double nutrientValue = food.getNutrients().getOrDefault(targetNutrient, 0.0);
                                    totalValue += nutrientValue * (quantity / 100.0);
                                    System.out.println("DEBUG: Food " + foodId + " " + targetNutrient + ": " + nutrientValue + " * " + quantity + "/100 = " + (nutrientValue * quantity / 100.0));
                                    System.out.println("DEBUG: Food " + foodId + " name: " + food.getName() + ", all nutrients: " + food.getNutrients());
                                } else {
                                    System.out.println("DEBUG: Food " + foodId + " not found in database or has no nutrients");
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Error parsing food data: " + foodData[0] + "," + foodData[1]);
                            }
                        }
                    }
                }
                
                System.out.println("DEBUG: Total original " + targetNutrient + " value: " + totalValue);
                return totalValue;
            }
            
            // Handle full format: "MealID:123;UserID:1;Date:2025-07-27;Type:BREAKFAST;Ingredients:123,100;456,200;"
            if (originalData.contains("Ingredients:")) {
                String[] parts = originalData.split(";");
                double totalValue = 0.0;
                
                for (String part : parts) {
                    if (part.startsWith("Ingredients:")) {
                        String ingredientsPart = part.substring(12); // Remove "Ingredients:"
                        String[] ingredientData = ingredientsPart.split(",");
                        
                        // Process pairs of foodId,quantity
                        for (int i = 0; i < ingredientData.length - 1; i += 2) {
                            try {
                                int foodId = Integer.parseInt(ingredientData[i]);
                                double quantity = Double.parseDouble(ingredientData[i + 1]);
                                
                                model.FoodItem food = foodDatabase.get(foodId);
                                if (food != null && food.getNutrients() != null) {
                                    double nutrientValue = food.getNutrients().getOrDefault(targetNutrient, 0.0);
                                    totalValue += nutrientValue * (quantity / 100.0);
                                    System.out.println("DEBUG: Food " + foodId + " " + targetNutrient + ": " + nutrientValue + " * " + quantity + "/100 = " + (nutrientValue * quantity / 100.0));
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Error parsing ingredient data: " + ingredientData[i] + "," + ingredientData[i + 1]);
                            }
                        }
                        break; // Found ingredients, no need to check other parts
                    }
                }
                
                System.out.println("DEBUG: Total original " + targetNutrient + " value: " + totalValue);
                return totalValue;
            }
        } catch (Exception e) {
            System.err.println("Error parsing original nutrient value: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }

    /**
     * Save cumulative effects to database for a specific date
     */
    private void saveCumulativeEffectsToDatabase(LocalDate date, Map<String, Double> cumulativeEffects) {
        try {
            dao.adapter.MySQLAdapter adapter = new dao.adapter.MySQLAdapter();
            Connection conn = adapter.connect();
            
            if (conn != null) {
                // Create table if not exists
                String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS cumulative_effects (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL,
                        date DATE NOT NULL,
                        nutrient VARCHAR(50) NOT NULL,
                        effect_value DOUBLE NOT NULL,
                        UNIQUE KEY unique_user_date_nutrient (user_id, date, nutrient)
                    )
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {
                    stmt.executeUpdate();
                }
                
                // Save each nutrient effect
                String insertSQL = "INSERT INTO cumulative_effects (user_id, date, nutrient, effect_value) VALUES (?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE effect_value = VALUES(effect_value)";
                
                try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                    for (Map.Entry<String, Double> entry : cumulativeEffects.entrySet()) {
                        stmt.setInt(1, userId);
                        stmt.setDate(2, Date.valueOf(date));
                        stmt.setString(3, entry.getKey());
                        stmt.setDouble(4, entry.getValue());
                        stmt.executeUpdate();
                        
                        System.out.println("DEBUG: Saved " + entry.getKey() + " effect: " + entry.getValue() + " for date " + date);
                    }
                }
                
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error saving cumulative effects: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load cumulative effects from database for a specific date
     */
    private Map<String, Double> loadCumulativeEffectsFromDatabase(LocalDate date) {
        Map<String, Double> cumulativeEffects = new HashMap<>();
        
        try {
            dao.adapter.MySQLAdapter adapter = new dao.adapter.MySQLAdapter();
            Connection conn = adapter.connect();
            
            if (conn != null) {
                String sql = "SELECT nutrient, effect_value FROM cumulative_effects WHERE user_id = ? AND date = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setDate(2, Date.valueOf(date));
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String nutrient = rs.getString("nutrient");
                            double effectValue = rs.getDouble("effect_value");
                            cumulativeEffects.put(nutrient, effectValue);
                            System.out.println("DEBUG: Loaded " + nutrient + " effect: " + effectValue + " for date " + date);
                        }
                    }
                }
                
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error loading cumulative effects: " + e.getMessage());
            e.printStackTrace();
        }
        
        return cumulativeEffects;
    }
    

    
    /**
     * Clear cumulative effects for a specific date
     */
    private void clearCumulativeEffectsForDate(LocalDate date) {
        try {
            dao.adapter.MySQLAdapter adapter = new dao.adapter.MySQLAdapter();
            Connection conn = adapter.connect();
            
            if (conn != null) {
                String sql = "DELETE FROM cumulative_effects WHERE user_id = ? AND date = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setDate(2, Date.valueOf(date));
                    
                    int deletedRows = stmt.executeUpdate();
                    System.out.println("DEBUG: Deleted " + deletedRows + " old cumulative effects for date " + date);
                }
                
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Error clearing cumulative effects: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    
    /**
     * Create a custom message dialog with English buttons
     */
    private void showEnglishMessageDialog(Component parent, String message, String title, int messageType) {
        // Find the top-level window
        Window window = SwingUtilities.getWindowAncestor(parent);
        if (window == null) {
            // Fallback: create a new frame
            window = new JFrame();
        }
        
        JDialog dialog = new JDialog((Frame) window, title, true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(window);
        
        // Message panel
        JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Icon based on message type
        JLabel iconLabel = new JLabel();
        iconLabel.setFont(new Font("Arial", Font.BOLD, 24));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        switch (messageType) {
            case JOptionPane.ERROR_MESSAGE:
                iconLabel.setText("✗");
                iconLabel.setForeground(new Color(255, 0, 0));
                break;
            case JOptionPane.WARNING_MESSAGE:
                iconLabel.setText("⚠");
                iconLabel.setForeground(new Color(255, 165, 0));
                break;
            case JOptionPane.INFORMATION_MESSAGE:
            default:
                iconLabel.setText("ℹ");
                iconLabel.setForeground(new Color(0, 128, 255));
                break;
        }
        
        JLabel messageLabel = new JLabel("<html><center>" + message.replace("\n", "<br>") + "</center></html>");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        messagePanel.add(iconLabel, BorderLayout.NORTH);
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(okButton);
        
        dialog.add(messagePanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}


