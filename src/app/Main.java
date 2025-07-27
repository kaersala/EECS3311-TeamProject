package app;

import view.*;
import model.*;
import model.meal.*;
import model.user.*;
import service.*;
import backend.*;
import controller.*;
import dao.Implementations.*;
import dao.interfaces.IGoalDAO;
import dao.adapter.*;
import chart.*;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class Main {
    private static UserProfile currentUser;
    private static List<Goal> userGoals;
    private static MealDAO mealDAO;
    private static FoodDAO foodDAO;
    private static UserProfileDAO userProfileDAO;
    private static IGoalDAO goalDAO;
    private static Map<Integer, FoodItem> foodDatabase;
    private static JFrame mainFrame;
    private static SwapService swapService;
    
    public static void main(String[] args) {
        // Set English locale for the entire application
        Locale.setDefault(Locale.ENGLISH);
        
        // Initialize the application
        initializeApplication();
        
        // Start with splash screen
        SwingUtilities.invokeLater(() -> {
            showSplashScreen();
        });
    }
    
    private static void initializeApplication() {
        // Initialize database adapter
        DatabaseAdapter databaseAdapter = new MySQLAdapter();
        Connection connection = databaseAdapter.connect();
        
        if (connection != null) {
            // Initialize DAOs with database connection
            mealDAO = new MealDAO();
            foodDAO = new FoodDAO(connection);
            userProfileDAO = new UserProfileDAO();
            goalDAO = new GoalDAO();
            
            // Set database adapter for SwapService
            swapService = new SwapService();
            swapService.setDatabaseAdapter(databaseAdapter);
            
            System.out.println("Database connection established successfully");
        } else {
            // Fallback to in-memory data if database connection fails
            System.err.println("Warning: Database connection failed, using in-memory data");
            mealDAO = new MealDAO();
            foodDAO = new FoodDAO(null);
            userProfileDAO = new UserProfileDAO();
            goalDAO = new GoalDAO();
            
            // Initialize SwapService even without database connection
            swapService = new SwapService();
            System.err.println("Warning: SwapService initialized without database adapter");
            
            // Load food database
            loadFoodDatabase();
        }
        
        // Initialize services
        initializeServices();
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
        // Create splash screen with callback to continue to main menu
        SplashScreenUI splashScreen = new SplashScreenUI(() -> {
            // This will be called when a profile is selected
            // Get the current user from UserProfileManager
            UserProfileManager profileManager = UserProfileManager.getInstance();
            currentUser = profileManager.getCurrentProfile();
            
            if (currentUser != null) {
                // Load user's goals
                loadUserGoals();
                showMainMenu();
            } else {
                // If no current profile, show error and exit
                JOptionPane.showMessageDialog(null, "No profile selected. Please restart the application.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
        
        splashScreen.setVisible(true);
    }
    
    private static void loadUserGoals() {
        if (currentUser != null) {
            userGoals = goalDAO.loadGoals(currentUser.getUserID());
            System.out.println("Loaded " + userGoals.size() + " goals for user " + currentUser.getUserID());
        }
    }
    
    private static void showGoalSelection() {
        // Create frame for goal selection
        mainFrame = new JFrame("Select Nutritional Goals");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 400);
        mainFrame.setLocationRelativeTo(null);
        
        GoalSelectionUI goalSelectionUI = new GoalSelectionUI();
        
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
        // Create a new frame for goal selection (not replacing main frame)
        JFrame goalFrame = new JFrame("Set Nutritional Goals");
        goalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        goalFrame.setSize(600, 500);
        goalFrame.setLocationRelativeTo(null);
        
        // Pass existing goals so user can edit them
        GoalSelectionUI goalSelectionUI = new GoalSelectionUI(userGoals != null ? userGoals : new ArrayList<>());
        
        // Add a save button to update goals and return to main menu
        JButton saveBtn = new JButton("Save Goals");
        saveBtn.addActionListener(e -> {
            List<Goal> selectedGoals = goalSelectionUI.getSelectedGoals();
            if (!selectedGoals.isEmpty()) {
                // Save goals to database
                if (currentUser != null) {
                    goalDAO.saveGoals(currentUser.getUserID(), selectedGoals);
                    userGoals = selectedGoals;
                    JOptionPane.showMessageDialog(goalFrame, 
                        "Goals saved successfully!\n" +
                        "Your swap suggestions will now be based on these goals.", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(goalFrame, 
                        "Error: No user profile loaded.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(goalFrame, 
                    "Please add at least one goal using the 'Add Goal' button.", 
                    "No Goals Selected", 
                    JOptionPane.WARNING_MESSAGE);
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
        if (mainFrame != null) mainFrame.dispose();
        mainFrame = new JFrame("NutriSci - Main Menu");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(600, 400);
        mainFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome message
        String userName = currentUser != null ? currentUser.getName() : "User";
        JLabel welcomeLabel = new JLabel("Welcome, " + userName + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // Main menu buttons
        JPanel menuPanel = new JPanel(new GridLayout(6, 1, 15, 15));

        JButton goalsBtn = new JButton("Goals");
        JButton logMealBtn = new JButton("Log Meal");
        JButton journalBtn = new JButton("View Journal");
        JButton editProfileBtn = new JButton("Edit Profile");
        JButton switchUserBtn = new JButton("Switch User");
        JButton exitBtn = new JButton("Exit");

        // Smart switching: enter setup interface when no goals set, enter view interface when goals exist
        goalsBtn.addActionListener(e -> {
            if (userGoals == null || userGoals.isEmpty()) {
                showGoalSelectionFromMenu();
            } else {
                showGoalsDialog();
            }
        });
        logMealBtn.addActionListener(e -> showMealLogging());
        journalBtn.addActionListener(e -> showJournalPanel());
        editProfileBtn.addActionListener(e -> showProfileDialog());
        switchUserBtn.addActionListener(e -> switchToUserSelection());
        exitBtn.addActionListener(e -> System.exit(0));

        menuPanel.add(goalsBtn);
        menuPanel.add(logMealBtn);
        menuPanel.add(journalBtn);
        menuPanel.add(editProfileBtn);
        menuPanel.add(switchUserBtn);
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
        
        int userId = currentUser != null ? currentUser.getUserID() : 1;
        MealEntryPanel mealEntryPanel = new MealEntryPanel(userId);
        
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
        
        int userId = currentUser != null ? currentUser.getUserID() : 4; // Use user 4 as default since that's where the meal data is
        
        // Debug: Check swapService status
        System.out.println("=== showJournal() called ===");
        System.out.println("Global swapService: " + (swapService != null ? "NOT NULL" : "NULL"));
        
        // Use the global swapService instance if available, otherwise create a new one
        SwapService journalSwapService = swapService != null ? swapService : new SwapService();
        System.out.println("journalSwapService: " + (journalSwapService != null ? "NOT NULL" : "NULL"));
        
        // Get the database adapter that was used to initialize swapService
        DatabaseAdapter journalDatabaseAdapter = null;
        if (swapService != null) {
            // Try to get the database adapter from the existing connection
            try {
                journalDatabaseAdapter = new MySQLAdapter();
                journalDatabaseAdapter.connect();
                System.out.println("Created new DatabaseAdapter for JournalPanel");
            } catch (Exception e) {
                System.err.println("Failed to create DatabaseAdapter for JournalPanel: " + e.getMessage());
            }
        }
        
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
//swap
        JLabel titleLabel = new JLabel("Food Swap Suggestions", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        Map<Integer, FoodItem> foodDatabase = new HashMap<>();
        foodDatabase.put(1, new FoodItem(1, "Beef Steak", 250, Map.of("Calories", 250.0, "Protein", 26.0, "Fat", 15.0), "Meat"));
        foodDatabase.put(2, new FoodItem(2, "Chicken Breast", 165, Map.of("Calories", 165.0, "Protein", 31.0, "Fat", 3.6), "Meat"));
        foodDatabase.put(3, new FoodItem(3, "Lentils", 120, Map.of("Calories", 120.0, "Protein", 9.0, "Fiber", 8.0), "Legume"));

       
        List<IngredientEntry> currentMeal = List.of(new IngredientEntry(1, 100)); // 100g beef

      
        Goal goal = new Goal("Calories", "Decrease", 1.5, "High");
        List<Goal> goals = List.of(goal);

    
        SwapEngine engine = new SwapEngine();
        List<SwapSuggestion> suggestions = engine.generateSwaps(goals, currentMeal, foodDatabase);

       
        JTextArea swapArea = new JTextArea();
        swapArea.setEditable(false);
        swapArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

       
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
        
        JTextArea chartArea = new JTextArea();
        chartArea.setText("Daily Nutrition Breakdown:\n\n" +
                         "Calories: 1850 / 2000 (92%)\n" +
                         "Protein: 85g / 90g (94%)\n" +
                         "Fiber: 25g / 30g (83%)\n" +
                         "Fat: 65g / 70g (93%)\n" +
                         "Sodium: 1400mg / 1500mg (93%)");
        chartArea.setEditable(false);
        chartFrame.add(new JScrollPane(chartArea));
        chartFrame.setVisible(true);
    }
    
    private static void showWeeklyChart() {
        JFrame chartFrame = new JFrame("Weekly Trends");
        chartFrame.setSize(600, 400);
        chartFrame.setLocationRelativeTo(null);
        
        JTextArea trendArea = new JTextArea();
        trendArea.setText("Weekly Calorie Trends:\n\n" +
                         "Monday: 1920 calories\n" +
                         "Tuesday: 1850 calories\n" +
                         "Wednesday: 1780 calories\n" +
                         "Thursday: 1950 calories\n" +
                         "Friday: 1820 calories\n" +
                         "Saturday: 2100 calories\n" +
                         "Sunday: 1880 calories\n\n" +
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
            SwingChart swingChart = (SwingChart) chart;
            chartFrame.add(swingChart.getChartPanel());
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
        
        // Create custom dialog with edit button
        JDialog dialog = new JDialog(mainFrame, "Your Goals", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Goals text
        JTextArea goalsArea = new JTextArea(goalsText.toString());
        goalsArea.setEditable(false);
        goalsArea.setFont(new Font("Arial", Font.PLAIN, 12));
        goalsArea.setBackground(dialog.getBackground());
        goalsArea.setLineWrap(true);
        goalsArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(goalsArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton editBtn = new JButton("Edit Goals");
        JButton okBtn = new JButton("OK");
        
        editBtn.addActionListener(e -> {
            dialog.dispose();
            showGoalSelectionFromMenu();
        });
        
        okBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(editBtn);
        buttonPanel.add(okBtn);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    private static void showSettingsDialog() {
    	if (currentUser == null) {
            JOptionPane.showOptionDialog(mainFrame, "No user profile loaded. Cannot open settings.", "No Profile", 
                JOptionPane.WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE, null, 
                new String[]{"OK"}, "OK");
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
            
            EditProfilePanel profilePanel = new EditProfilePanel(currentUser);
            profileFrame.add(profilePanel);
            profileFrame.setVisible(true);
        } else {
            JOptionPane.showOptionDialog(mainFrame, "No profile loaded.", "Profile", 
                JOptionPane.WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE, null, 
                new String[]{"OK"}, "OK");
        }
    }

    private static void switchToUserSelection() {
        // Clear current user but don't remove from database
        currentUser = null;
        userGoals = null;
        UserProfileManager.getInstance().clearCurrentProfile();
        
        // Close main menu
        if (mainFrame != null) {
            mainFrame.dispose();
        }
        
        // Show splash screen again
        showSplashScreen();
    }

    // New: display journal panel
    private static void showJournalPanel() {
        JFrame journalFrame = new JFrame("Meal Journal");
        journalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        journalFrame.setSize(700, 500);
        journalFrame.setLocationRelativeTo(mainFrame);
        // Assume current user ID is 1, actual can be obtained from currentUser.getUserID()
        int userId = currentUser != null ? currentUser.getUserID() : 1;
        
        // Debug: Check swapService status
        System.out.println("=== showJournalPanel() called ===");
        System.out.println("Global swapService: " + (swapService != null ? "NOT NULL" : "NULL"));
        
        // Use the global swapService instance if available, otherwise create a new one
        SwapService journalSwapService = swapService != null ? swapService : new SwapService();
        System.out.println("journalSwapService: " + (journalSwapService != null ? "NOT NULL" : "NULL"));
        
        // Get the database adapter that was used to initialize swapService
        DatabaseAdapter journalDatabaseAdapter = null;
        if (swapService != null) {
            // Try to get the database adapter from the existing connection
            try {
                journalDatabaseAdapter = new MySQLAdapter();
                journalDatabaseAdapter.connect();
                System.out.println("Created new DatabaseAdapter for JournalPanel");
            } catch (Exception e) {
                System.err.println("Failed to create DatabaseAdapter for JournalPanel: " + e.getMessage());
            }
        }
        
        view.JournalPanel journalPanel = new view.JournalPanel(userId);
        journalFrame.add(journalPanel);
        journalFrame.setVisible(true);
    }
}
    