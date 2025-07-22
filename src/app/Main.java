package app;

import view.*;
import model.*;
import model.meal.*;
import model.user.*;
import service.*;
import backend.*;
import controller.*;
import dao.Implementations.*;
import chart.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class Main {
    private static UserProfile currentUser;
    private static List<Goal> userGoals;
    private static MealDAO mealDAO;
    private static FoodDAO foodDAO;
    private static UserProfileDAO userProfileDAO;
    private static Map<Integer, FoodItem> foodDatabase;
    private static JFrame mainFrame;
    
    public static void main(String[] args) {
        // Initialize the application
        initializeApplication();
        
        // Start with splash screen
        SwingUtilities.invokeLater(() -> {
            showSplashScreen();
        });
    }
    
    private static void initializeApplication() {
        // Initialize DAOs with null connection for demo (in real app, would use actual database connection)
        mealDAO = new MealDAO();
        foodDAO = new FoodDAO(null); // Will use in-memory data instead
        userProfileDAO = new UserProfileDAO();
        
        // Load food database
        loadFoodDatabase();
        
        // Initialize services
        initializeServices();
        
        System.out.println("NutriSci Application initialized successfully!");
    }
    
    private static void loadFoodDatabase() {
        foodDatabase = new HashMap<>();
        
        // Add sample food items (in real app, this would load from CSV/database)
        foodDatabase.put(1, new FoodItem(1, "Beef Steak", 250, 
            Map.of("Calories", 250.0, "Protein", 26.0, "Fat", 15.0, "Sodium", 70.0), "Meat"));
        foodDatabase.put(2, new FoodItem(2, "Chicken Breast", 165, 
            Map.of("Calories", 165.0, "Protein", 31.0, "Fat", 3.6, "Sodium", 74.0), "Meat"));
        foodDatabase.put(3, new FoodItem(3, "Salmon", 208, 
            Map.of("Calories", 208.0, "Protein", 25.0, "Fat", 12.0, "Omega-3", 2.3), "Fish"));
        foodDatabase.put(4, new FoodItem(4, "Brown Rice", 111, 
            Map.of("Calories", 111.0, "Protein", 2.6, "Fiber", 1.8, "Carbs", 23.0), "Grains"));
        foodDatabase.put(5, new FoodItem(5, "White Rice", 130, 
            Map.of("Calories", 130.0, "Protein", 2.7, "Fiber", 0.4, "Carbs", 28.0), "Grains"));
        foodDatabase.put(6, new FoodItem(6, "Broccoli", 55, 
            Map.of("Calories", 55.0, "Protein", 3.7, "Fiber", 5.2, "Vitamin C", 89.0), "Vegetables"));
        foodDatabase.put(7, new FoodItem(7, "Spinach", 23, 
            Map.of("Calories", 23.0, "Protein", 2.9, "Fiber", 2.2, "Iron", 2.7), "Vegetables"));
        foodDatabase.put(8, new FoodItem(8, "Apple", 95, 
            Map.of("Calories", 95.0, "Protein", 0.5, "Fiber", 4.4, "Vitamin C", 8.4), "Fruits"));
        foodDatabase.put(9, new FoodItem(9, "Banana", 105, 
            Map.of("Calories", 105.0, "Protein", 1.3, "Fiber", 3.1, "Potassium", 422.0), "Fruits"));
        foodDatabase.put(10, new FoodItem(10, "Greek Yogurt", 59, 
            Map.of("Calories", 59.0, "Protein", 10.0, "Fat", 0.4, "Calcium", 110.0), "Dairy"));
    }
    
    private static void initializeServices() {
        // Services will be initialized as needed
    }
    
    private static void showSplashScreen() {
        // Create sample profiles for demo
        List<String> profiles = Arrays.asList("Alice Smith", "Bob Johnson", "Charlie Lee");
        
        // Create splash screen with callback to continue to main menu
        SplashScreenUI splashScreen = new SplashScreenUI(profiles, () -> {
            // This will be called when a profile is selected
            // Create a default user and go directly to main menu
            currentUser = new UserProfile("Demo User", "Male", LocalDate.of(1990, 1, 1), 175.0, 70.0);
            currentUser.setUserID(1);
            showMainMenu();
        });
        
        splashScreen.setVisible(true);
    }
    
    private static void showGoalSelection() {
        // Sample predefined goals
        List<Goal> goals = new ArrayList<>();
        goals.add(new Goal("Fiber", "Increase", 2.0, ""));
        goals.add(new Goal("Calories", "Decrease", 10.0, ""));
        goals.add(new Goal("Sodium", "Decrease", 1.5, ""));
        goals.add(new Goal("Protein", "Increase", 5.0, ""));
        goals.add(new Goal("Fat", "Decrease", 3.0, ""));
        goals.add(new Goal("Carbohydrates", "Decrease", 5.0, ""));

        // Create frame for goal selection
        mainFrame = new JFrame("Select Nutritional Goals");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 400);
        mainFrame.setLocationRelativeTo(null);
        
        GoalSelectionUI goalSelectionUI = new GoalSelectionUI(goals);
        
        // Add a continue button to proceed to main menu
        JButton continueBtn = new JButton("Continue to Main Menu");
        continueBtn.addActionListener(e -> {
            userGoals = goalSelectionUI.getSelectedGoals();
            if (userGoals.isEmpty()) {
                // Create a default user if no goals selected
                currentUser = new UserProfile("Demo User", "Male", LocalDate.of(1990, 1, 1), 175.0, 70.0);
                currentUser.setUserID(1);
            }
            showMainMenu();
        });
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(goalSelectionUI, BorderLayout.CENTER);
        panel.add(continueBtn, BorderLayout.SOUTH);
        
        mainFrame.add(panel);
        mainFrame.setVisible(true);
    }
    
    private static void showGoalSelectionFromMenu() {
        // Sample predefined goals
        List<Goal> goals = new ArrayList<>();
        goals.add(new Goal("Fiber", "Increase", 2.0, ""));
        goals.add(new Goal("Calories", "Decrease", 10.0, ""));
        goals.add(new Goal("Sodium", "Decrease", 1.5, ""));
        goals.add(new Goal("Protein", "Increase", 5.0, ""));
        goals.add(new Goal("Fat", "Decrease", 3.0, ""));
        goals.add(new Goal("Carbohydrates", "Decrease", 5.0, ""));

        // Create a new frame for goal selection (not replacing main frame)
        JFrame goalFrame = new JFrame("Set Nutritional Goals");
        goalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        goalFrame.setSize(500, 400);
        goalFrame.setLocationRelativeTo(null);
        
        GoalSelectionUI goalSelectionUI = new GoalSelectionUI(goals);
        
        // Add a save button to update goals and return to main menu
        JButton saveBtn = new JButton("Save Goals");
        saveBtn.addActionListener(e -> {
            userGoals = goalSelectionUI.getSelectedGoals();
            if (!userGoals.isEmpty()) {
                JOptionPane.showMessageDialog(goalFrame, "Goals updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(goalFrame, "Please select at least one goal.", "No Goals Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            goalFrame.dispose(); // Close the goal selection window
        });
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(goalSelectionUI, BorderLayout.CENTER);
        panel.add(saveBtn, BorderLayout.SOUTH);
        
        goalFrame.add(panel);
        goalFrame.setVisible(true);
    }
    
    private static void showMainMenu() {
        // Only dispose if mainFrame already exists
        if (mainFrame != null) {
            mainFrame.dispose();
        }
        
        mainFrame = new JFrame("NutriSci - Main Menu");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(700, 500);
        mainFrame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Welcome message
        String userName = currentUser != null ? currentUser.getName() : "User";
        JLabel welcomeLabel = new JLabel("Welcome, " + userName + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(welcomeLabel, BorderLayout.NORTH);
        
        // Main menu buttons
        JPanel menuPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        
        JButton logMealBtn = new JButton("Log Meal");
        JButton viewJournalBtn = new JButton("View Journal");
        JButton swapFoodBtn = new JButton("Swap Food Items");
        JButton viewChartsBtn = new JButton("View Charts");
        JButton viewGoalsBtn = new JButton("View Goals");
        JButton setGoalsBtn = new JButton("Set Goals");
        JButton profileBtn = new JButton("Edit Profile");
        JButton exitBtn = new JButton("Exit");
        
        logMealBtn.addActionListener(e -> showMealLogging());
        viewJournalBtn.addActionListener(e -> showJournal());
        swapFoodBtn.addActionListener(e -> showFoodSwap());
        viewChartsBtn.addActionListener(e -> showCharts());
        viewGoalsBtn.addActionListener(e -> showGoalsDialog());
        setGoalsBtn.addActionListener(e -> showGoalSelectionFromMenu());
        profileBtn.addActionListener(e -> showProfileDialog());
        exitBtn.addActionListener(e -> System.exit(0));
        
        menuPanel.add(logMealBtn);
        menuPanel.add(viewJournalBtn);
        menuPanel.add(swapFoodBtn);
        menuPanel.add(viewChartsBtn);
        menuPanel.add(viewGoalsBtn);
        menuPanel.add(setGoalsBtn);
        menuPanel.add(profileBtn);
        menuPanel.add(exitBtn);
        
        panel.add(menuPanel, BorderLayout.CENTER);
        mainFrame.add(panel);
        mainFrame.setVisible(true);
    }
    
    private static void showMealLogging() {
        JFrame mealFrame = new JFrame("Log Meal");
        mealFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mealFrame.setSize(800, 600);
        mealFrame.setLocationRelativeTo(null);
        
        MealEntryPanel mealEntryPanel = new MealEntryPanel();
        
        // Add a back button
        JButton backBtn = new JButton("Back to Main Menu");
        backBtn.addActionListener(e -> mealFrame.dispose());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(mealEntryPanel, BorderLayout.CENTER);
        panel.add(backBtn, BorderLayout.SOUTH);
        
        mealFrame.add(panel);
        mealFrame.setVisible(true);
    }
    
    private static void showJournal() {
        JFrame journalFrame = new JFrame("Food Journal");
        journalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        journalFrame.setSize(800, 600);
        journalFrame.setLocationRelativeTo(null);
        
        int userId = currentUser != null ? currentUser.getUserID() : 1;
        JournalPanel journalPanel = new JournalPanel(userId);
        
        // Add a back button
        JButton backBtn = new JButton("Back to Main Menu");
        backBtn.addActionListener(e -> journalFrame.dispose());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(journalPanel, BorderLayout.CENTER);
        panel.add(backBtn, BorderLayout.SOUTH);
        
        journalFrame.add(panel);
        journalFrame.setVisible(true);
    }
    
    private static void showFoodSwap() {
        JFrame swapFrame = new JFrame("Food Swap");
        swapFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        swapFrame.setSize(800, 600);
        swapFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Food Swap Suggestions", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 1. Sample food database
        Map<Integer, FoodItem> foodDatabase = new HashMap<>();
        foodDatabase.put(1, new FoodItem(1, "Beef Steak", 250, Map.of("Calories", 250.0, "Protein", 26.0, "Fat", 15.0), "Meat"));
        foodDatabase.put(2, new FoodItem(2, "Chicken Breast", 165, Map.of("Calories", 165.0, "Protein", 31.0, "Fat", 3.6), "Meat"));
        foodDatabase.put(3, new FoodItem(3, "Lentils", 120, Map.of("Calories", 120.0, "Protein", 9.0, "Fiber", 8.0), "Legume"));

        // 2. Create a sample meal (e.g., 100g beef)
        List<IngredientEntry> currentMeal = List.of(new IngredientEntry(1, 100)); // 100g beef

        // 3. Create a hardcoded goal (decrease calories with "High" intensity)
        Goal goal = new Goal("Calories", "Decrease", 10.0, "High");
        List<Goal> goals = List.of(goal);

        // 4. Generate swap suggestions
        SwapEngine engine = new SwapEngine();
        List<SwapSuggestion> suggestions = engine.generateSwaps(goals, currentMeal, foodDatabase);

        // ✅ NEW: Create a JTextArea to display suggestions
        JTextArea swapArea = new JTextArea();
        swapArea.setEditable(false);
        swapArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // 5. Populate swap suggestions
        StringBuilder sb = new StringBuilder("Swap Suggestions:\n\n");
        for (SwapSuggestion s : suggestions) {
            FoodItem original = foodDatabase.get(s.getOriginal().getFoodID());
            FoodItem replacement = foodDatabase.get(s.getReplacement().getFoodID());
            sb.append("• Replace ")
              .append(original.getName())
              .append(" with ")
              .append(replacement.getName())
              .append(" → ")
              .append(s.getReason())
              .append("\n");
        }
        swapArea.setText(sb.toString());

        // ✅ Add the text area to scroll pane and panel
        JScrollPane scrollPane = new JScrollPane(swapArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        swapFrame.add(panel);
        swapFrame.setVisible(true);
    }

    
    private static void showCharts() {
        JFrame chartsFrame = new JFrame("Nutrition Charts");
        chartsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartsFrame.setSize(800, 600);
        chartsFrame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Nutrition Charts", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Chart options
        JPanel chartPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JButton dailyChartBtn = new JButton("Daily Nutrition");
        JButton weeklyChartBtn = new JButton("Weekly Trends");
        JButton goalChartBtn = new JButton("Goal Progress");
        JButton cfgChartBtn = new JButton("CFG Compliance");
        
        dailyChartBtn.addActionListener(e -> showDailyChart());
        weeklyChartBtn.addActionListener(e -> showWeeklyChart());
        goalChartBtn.addActionListener(e -> showGoalChart());
        cfgChartBtn.addActionListener(e -> showCFGChart());
        
        chartPanel.add(dailyChartBtn);
        chartPanel.add(weeklyChartBtn);
        chartPanel.add(goalChartBtn);
        chartPanel.add(cfgChartBtn);
        
        panel.add(chartPanel, BorderLayout.CENTER);
        
        JButton backBtn = new JButton("Back to Main Menu");
        backBtn.addActionListener(e -> chartsFrame.dispose());
        panel.add(backBtn, BorderLayout.SOUTH);
        
        chartsFrame.add(panel);
        chartsFrame.setVisible(true);
    }
    
    private static void showDailyChart() {
        JFrame chartFrame = new JFrame("Daily Nutrition");
        chartFrame.setSize(600, 400);
        chartFrame.setLocationRelativeTo(null);
        
        // Create sample data
        Map<String, Double> dailyData = new HashMap<>();
        dailyData.put("Calories", 1850.0);
        dailyData.put("Protein", 85.0);
        dailyData.put("Fiber", 25.0);
        dailyData.put("Fat", 65.0);
        
        // Create chart
        NutrientChartFactory factory = new NutrientChartFactory();
        Chart chart = factory.createDailyNutrientChart(dailyData);
        
        if (chart instanceof SwingChart) {
            chartFrame.add(((SwingChart) chart).getChartPanel());
        }
        
        chartFrame.setVisible(true);
    }
    
    private static void showWeeklyChart() {
        JFrame chartFrame = new JFrame("Weekly Trends");
        chartFrame.setSize(600, 400);
        chartFrame.setLocationRelativeTo(null);
        
        JTextArea trendArea = new JTextArea();
        trendArea.setText("Weekly Nutrition Trends:\n\n" +
                         "Monday: 1850 calories\n" +
                         "Tuesday: 1920 calories\n" +
                         "Wednesday: 1780 calories\n" +
                         "Thursday: 1950 calories\n" +
                         "Friday: 1820 calories\n" +
                         "Saturday: 2100 calories\n" +
                         "Sunday: 1750 calories\n\n" +
                         "Average: 1881 calories");
        trendArea.setEditable(false);
        chartFrame.add(new JScrollPane(trendArea));
        chartFrame.setVisible(true);
    }
    
    private static void showGoalChart() {
        JFrame chartFrame = new JFrame("Goal Progress");
        chartFrame.setSize(600, 400);
        chartFrame.setLocationRelativeTo(null);
        
        JTextArea goalArea = new JTextArea();
        goalArea.setText("Goal Progress:\n\n" +
                        "Fiber Goal: 25g (Target: 30g) - 83% complete\n" +
                        "Protein Goal: 85g (Target: 90g) - 94% complete\n" +
                        "Calories Goal: 1850 (Target: 2000) - 92% complete\n\n" +
                        "Overall Progress: 90%");
        goalArea.setEditable(false);
        chartFrame.add(new JScrollPane(goalArea));
        chartFrame.setVisible(true);
    }
    
    private static void showCFGChart() {
        JFrame chartFrame = new JFrame("CFG Compliance");
        chartFrame.setSize(600, 400);
        chartFrame.setLocationRelativeTo(null);
        
        // Create sample CFG data
        Map<String, Double> cfgData = new HashMap<>();
        cfgData.put("Vegetables", 30.0);
        cfgData.put("Grains", 25.0);
        cfgData.put("Protein", 25.0);
        cfgData.put("Fruits", 20.0);
        
        // Create chart
        CFGChartFactory factory = new CFGChartFactory();
        Chart chart = factory.createPlateChart(cfgData);
        
        if (chart instanceof SwingChart) {
            chartFrame.add(((SwingChart) chart).getChartPanel());
        }
        
        chartFrame.setVisible(true);
    }
    
    private static void showGoalsDialog() {
        StringBuilder goalsText = new StringBuilder("Your Current Goals:\n\n");
        
        if (userGoals != null && !userGoals.isEmpty()) {
            for (Goal goal : userGoals) {
                goalsText.append("• ").append(goal.getNutrient())
                        .append(": ").append(goal.getDirection())
                        .append(" by ").append(goal.getAmount())
                        .append(" (").append(goal.getIntensity()).append(")\n");
            }
        } else {
            goalsText.append("No goals set. You can set goals from the main menu.");
        }
        
        JOptionPane.showMessageDialog(mainFrame, goalsText.toString(), "Your Goals", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private static void showSettingsDialog() {
    	if (currentUser == null) {
            JOptionPane.showMessageDialog(mainFrame, "No user profile loaded. Cannot open settings.");
            return;
        }

        // Set current user in the manager (required!)
        UserProfileManager.getInstance().setCurrentProfile(currentUser.getUserID());

        JFrame settingsFrame = new JFrame("Edit Settings");
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setSize(400, 300);
        settingsFrame.setLocationRelativeTo(null);

        UserProfileController userController = new UserProfileController();
        EditSettingsPanel settingsPanel = new EditSettingsPanel(userController);
        settingsFrame.add(settingsPanel);
        settingsFrame.setVisible(true);
    }

    
    private static void showProfileDialog() {
        if (currentUser != null) {
            JFrame profileFrame = new JFrame("Edit Profile");
            profileFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            profileFrame.setSize(400, 400);
            profileFrame.setLocationRelativeTo(null);
            
            EditProfilePanel profilePanel = new EditProfilePanel();
            profileFrame.add(profilePanel);
            profileFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(mainFrame, "No profile loaded.", "Profile", JOptionPane.WARNING_MESSAGE);
        }
    }
}
    