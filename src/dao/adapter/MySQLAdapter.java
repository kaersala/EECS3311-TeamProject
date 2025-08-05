package dao.adapter;

import model.meal.Meal;
import model.meal.MealType;
import model.meal.IngredientEntry;
import model.user.UserProfile;
import model.FoodItem;
import model.Nutrient;
import java.sql.*;
import java.util.*;

public class MySQLAdapter implements DatabaseAdapter {
    private Connection connection;

    @Override
    public Connection connect() {
        // Load MySQL driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Please ensure mysql-connector-java is in the classpath.");
            System.err.println("Error: " + e.getMessage());
            return null;
        }
        
        // Use configuration class to get database connection information
        String url = DatabaseConfig.getDatabaseUrl();
        String user = DatabaseConfig.getUsername();
        String password = DatabaseConfig.getPassword();
        
        // Print configuration information (for debugging)
        DatabaseConfig.printConfig();
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("MySQL connection established successfully.");
            return connection;
        } catch (SQLException e) {
            System.err.println("Failed to connect to MySQL database: " + e.getMessage());
            System.err.println("Please check:");
            System.err.println("1. MySQL server is running on localhost:3306");
            System.err.println("2. Database 'cnf2015' exists");
            System.err.println("3. User 'root' with password 'abcd1234' has access");
            System.err.println("4. MySQL JDBC driver is in the classpath");
            return null;
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void saveMeal(Meal meal) {
        String mealInsert = "INSERT INTO meal (UserID, Date, Type) VALUES (?, ?, ?)";
        String ingredientInsert = "INSERT INTO ingredient (MealID, FoodID, Quantity) VALUES (?, ?, ?)";
        
        try {
            // Create a new connection for this operation
            Connection conn = connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for saving meal");
                return;
            }
            
            try (
                    PreparedStatement mealStmt = conn.prepareStatement(mealInsert, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement ingredientStmt = conn.prepareStatement(ingredientInsert)
            ) {
                mealStmt.setInt(1, meal.getUserID());
                mealStmt.setDate(2, java.sql.Date.valueOf(meal.getDate()));
                mealStmt.setString(3, meal.getType().name());
                mealStmt.executeUpdate();
                try (ResultSet generatedKeys = mealStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int mealId = generatedKeys.getInt(1);
                        for (IngredientEntry entry : meal.getIngredients()) {
                            ingredientStmt.setInt(1, mealId);
                            ingredientStmt.setInt(2, entry.getFoodID());
                            ingredientStmt.setDouble(3, entry.getQuantity());
                            ingredientStmt.addBatch();
                        }
                        ingredientStmt.executeBatch();
                        System.out.println("Saved meal: " + meal.getIngredients().get(0).getFoodID() + 
                                         " (" + meal.getIngredients().get(0).getQuantity() + "g, " + meal.getType() + ")");
                    }
                }
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving meal: " + e.getMessage());
        }
    }

    @Override
    public void deleteMeal(int mealId) {
        String deleteIngredients = "DELETE FROM ingredient WHERE MealID = ?";
        String deleteMeal = "DELETE FROM meal WHERE MealID = ?";
        
        try {
            // Create a new connection for this operation
            Connection conn = connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for deleting meal");
                return;
            }
            
            try (
                    PreparedStatement ingredientStmt = conn.prepareStatement(deleteIngredients);
                    PreparedStatement mealStmt = conn.prepareStatement(deleteMeal)
            ) {
                // First delete ingredients (due to foreign key constraint)
                ingredientStmt.setInt(1, mealId);
                ingredientStmt.executeUpdate();
                
                // Then delete the meal
                mealStmt.setInt(1, mealId);
                mealStmt.executeUpdate();
                
                System.out.println("Deleted meal ID: " + mealId);
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting meal: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteMealsByDate(int userId, String date) {
        // First get all meal IDs for the given date and user
        String getMealIds = "SELECT MealID FROM meal WHERE UserID = ? AND Date = ?";
        String deleteIngredients = "DELETE FROM ingredient WHERE MealID = ?";
        String deleteMeal = "DELETE FROM meal WHERE MealID = ?";
        
        try {
            // Create a new connection for this operation
            Connection conn = connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for deleting meals by date");
                return;
            }
            
            try (
                    PreparedStatement getMealIdsStmt = conn.prepareStatement(getMealIds);
                    PreparedStatement ingredientStmt = conn.prepareStatement(deleteIngredients);
                    PreparedStatement mealStmt = conn.prepareStatement(deleteMeal)
            ) {
                getMealIdsStmt.setInt(1, userId);
                getMealIdsStmt.setDate(2, java.sql.Date.valueOf(date));
                ResultSet rs = getMealIdsStmt.executeQuery();
                
                List<Integer> mealIds = new ArrayList<>();
                while (rs.next()) {
                    mealIds.add(rs.getInt("MealID"));
                }
                
                // Delete ingredients and meals for each meal ID
                for (Integer mealId : mealIds) {
                    // First delete ingredients (due to foreign key constraint)
                    ingredientStmt.setInt(1, mealId);
                    ingredientStmt.executeUpdate();
                    
                    // Then delete the meal
                    mealStmt.setInt(1, mealId);
                    mealStmt.executeUpdate();
                }
                
                System.out.println("Deleted " + mealIds.size() + " meals for user " + userId + " on date " + date);
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting meals by date: " + e.getMessage());
        }
    }

    @Override
    public void updateIngredientQuantity(int mealId, int foodId, double newQuantity) {
        String updateQuery = "UPDATE ingredient SET Quantity = ? WHERE MealID = ? AND FoodID = ?";
        
        try {
            // Create a new connection for this operation
            Connection conn = connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for updating ingredient quantity");
                return;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setDouble(1, newQuantity);
                stmt.setInt(2, mealId);
                stmt.setInt(3, foodId);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Successfully updated quantity for meal " + mealId + ", food " + foodId + " to " + newQuantity);
                } else {
                    System.err.println("No rows updated for meal " + mealId + ", food " + foodId);
                }
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating ingredient quantity: " + e.getMessage());
        }
    }

    @Override
    public void updateMeal(Meal meal) {
        String mealUpdate = "UPDATE meal SET UserID = ?, Date = ?, Type = ? WHERE MealID = ?";
        String deleteIngredients = "DELETE FROM ingredient WHERE MealID = ?";
        String ingredientInsert = "INSERT INTO ingredient (MealID, FoodID, Quantity) VALUES (?, ?, ?)";
        
        try {
            // Create a new connection for this operation
            Connection conn = connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for updating meal");
                return;
            }
            
            try (
                    PreparedStatement mealStmt = conn.prepareStatement(mealUpdate);
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteIngredients);
                    PreparedStatement ingredientStmt = conn.prepareStatement(ingredientInsert)
            ) {
                // Update meal details
                mealStmt.setInt(1, meal.getUserID());
                mealStmt.setDate(2, java.sql.Date.valueOf(meal.getDate()));
                mealStmt.setString(3, meal.getType().name());
                mealStmt.setInt(4, meal.getMealID());
                mealStmt.executeUpdate();
                
                // Delete existing ingredients
                deleteStmt.setInt(1, meal.getMealID());
                deleteStmt.executeUpdate();
                
                // Insert new ingredients
                for (IngredientEntry entry : meal.getIngredients()) {
                    ingredientStmt.setInt(1, meal.getMealID());
                    ingredientStmt.setInt(2, entry.getFoodID());
                    ingredientStmt.setDouble(3, entry.getQuantity());
                    ingredientStmt.addBatch();
                }
                ingredientStmt.executeBatch();
                
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating meal: " + e.getMessage());
        }
    }

    @Override
    public List<Meal> loadMeals(int userId) {
        List<Meal> meals = new ArrayList<>();
        String mealQuery = "SELECT * FROM meal WHERE UserID = ? ORDER BY Date DESC";
        String ingredientQuery = "SELECT FoodID, Quantity FROM ingredient WHERE MealID = ?";
        
        try {
            // Create a new connection for this operation
            Connection conn = connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for loading meals");
                return meals;
            }
            
            try (
                    PreparedStatement mealStmt = conn.prepareStatement(mealQuery);
                    PreparedStatement ingredientStmt = conn.prepareStatement(ingredientQuery)
            ) {
                mealStmt.setInt(1, userId);
                ResultSet mealRs = mealStmt.executeQuery();
                while (mealRs.next()) {
                    int mealId = mealRs.getInt("MealID");
                    java.time.LocalDate date = mealRs.getDate("Date").toLocalDate();
                    MealType type = MealType.valueOf(mealRs.getString("Type"));
                    List<IngredientEntry> ingredients = new ArrayList<>();
                    ingredientStmt.setInt(1, mealId);
                    ResultSet ingRs = ingredientStmt.executeQuery();
                    while (ingRs.next()) {
                        int foodId = ingRs.getInt("FoodID");
                        double quantity = ingRs.getDouble("Quantity");
                        
                        // Check if this food ID exists in the database
                        String foodCheckQuery = "SELECT COUNT(*) FROM food_name WHERE FoodID = ?";
                        try (PreparedStatement checkStmt = conn.prepareStatement(foodCheckQuery)) {
                            checkStmt.setInt(1, foodId);
                            ResultSet checkRs = checkStmt.executeQuery();
                            if (checkRs.next() && checkRs.getInt(1) == 0) {
                                System.err.println("Warning: Food item with ID " + foodId + " not found in database, skipping...");
                                continue; // Skip this ingredient
                            }
                        } catch (SQLException e) {
                            System.err.println("Error checking food ID " + foodId + ": " + e.getMessage());
                            continue;
                        }
                        
                        ingredients.add(new IngredientEntry(foodId, quantity));
                    }
                    meals.add(new Meal(mealId, userId, date, type, ingredients));
                }
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading meals: " + e.getMessage());
        }
        return meals;
    }

    @Override
    public void saveProfile(UserProfile profile) {
        String insertProfileQuery = "INSERT INTO user_profile (UserName, Sex, Dob, Height, Weight) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertProfileQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, profile.getName());
            stmt.setString(2, profile.getSex());
            stmt.setDate(3, java.sql.Date.valueOf(profile.getDob()));
            stmt.setDouble(4, profile.getHeight());
            stmt.setDouble(5, profile.getWeight());
            stmt.executeUpdate();
            
            // Get the generated UserID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    profile.setUserID(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving profile: " + e.getMessage());
        }
    }

    public void updateProfile(UserProfile profile) {
        String updateProfileQuery = "UPDATE user_profile SET UserName = ?, Sex = ?, Dob = ?, Height = ?, Weight = ? WHERE UserID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateProfileQuery)) {
            stmt.setString(1, profile.getName());
            stmt.setString(2, profile.getSex());
            stmt.setDate(3, java.sql.Date.valueOf(profile.getDob()));
            stmt.setDouble(4, profile.getHeight());
            stmt.setDouble(5, profile.getWeight());
            stmt.setInt(6, profile.getUserID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
        }
    }

    public void deleteProfile(int userId) {
        try {
            // Start transaction
            connection.setAutoCommit(false);
            
            // First, get all meal IDs for this user
            String getMealIdsQuery = "SELECT MealID FROM meal WHERE UserID = ?";
            List<Integer> mealIds = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(getMealIdsQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    mealIds.add(rs.getInt("MealID"));
                }
            }
            
            // Delete ingredients for each meal first (due to foreign key constraint)
            if (!mealIds.isEmpty()) {
                String deleteIngredientsQuery = "DELETE FROM ingredient WHERE MealID = ?";
                try (PreparedStatement stmt = connection.prepareStatement(deleteIngredientsQuery)) {
                    for (Integer mealId : mealIds) {
                        stmt.setInt(1, mealId);
                        int ingredientsDeleted = stmt.executeUpdate();
                        System.out.println("Deleted " + ingredientsDeleted + " ingredients for meal " + mealId);
                    }
                }
            }
            
            // Now delete all meals for this user
            String deleteMealsQuery = "DELETE FROM meal WHERE UserID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteMealsQuery)) {
                stmt.setInt(1, userId);
                int mealsDeleted = stmt.executeUpdate();
                System.out.println("Deleted " + mealsDeleted + " meals for user " + userId);
            }
            
            // Delete swap status records (has ON DELETE CASCADE, but being explicit)
            String deleteSwapStatusQuery = "DELETE FROM swap_status WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteSwapStatusQuery)) {
                stmt.setInt(1, userId);
                int swapStatusDeleted = stmt.executeUpdate();
                System.out.println("Deleted " + swapStatusDeleted + " swap status records for user " + userId);
            }
            
            // Delete user goals (has ON DELETE CASCADE, but being explicit)
            String deleteGoalsQuery = "DELETE FROM user_goals WHERE UserID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteGoalsQuery)) {
                stmt.setInt(1, userId);
                int goalsDeleted = stmt.executeUpdate();
                System.out.println("Deleted " + goalsDeleted + " goals for user " + userId);
            }
            
            // Finally, delete the user profile
            String deleteProfileQuery = "DELETE FROM user_profile WHERE UserID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteProfileQuery)) {
                stmt.setInt(1, userId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Profile with UserID " + userId + " deleted successfully.");
                    connection.commit();
                } else {
                    System.out.println("No profile found with UserID " + userId);
                    connection.rollback();
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.err.println("Error deleting profile: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    @Override
    public List<UserProfile> loadProfiles() {
        List<UserProfile> profiles = new ArrayList<>();
        
        // Check if connection is available
        if (connection == null) {
            System.err.println("Warning: No database connection available. Returning empty profile list.");
            return profiles;
        }
        
        String profileQuery = "SELECT * FROM user_profile";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(profileQuery)) {
            while (rs.next()) {
                int userId = rs.getInt("UserID");
                String name = rs.getString("UserName");
                String sex = rs.getString("Sex");
                java.time.LocalDate dob = rs.getDate("Dob").toLocalDate();
                double height = rs.getDouble("Height");
                double weight = rs.getDouble("Weight");
                UserProfile profile = new UserProfile(name, sex, dob, height, weight);
                profile.setUserID(userId);
                profiles.add(profile);
            }
        } catch (SQLException e) {
            System.err.println("Error loading profiles: " + e.getMessage());
            System.err.println("This might be due to database connection issues or missing tables.");
        } catch (Exception e) {
            System.err.println("Unexpected error loading profiles: " + e.getMessage());
        }
        return profiles;
    }

    @Override
    public List<IngredientEntry> loadIngredients() {
        List<IngredientEntry> ingredients = new ArrayList<>();
        String query = "SELECT FoodID, Quantity FROM meal_ingredient";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int foodId = rs.getInt("FoodID");
                double quantity = rs.getDouble("Quantity");
                ingredients.add(new IngredientEntry(foodId, quantity));
            }
        } catch (SQLException e) {
            System.err.println("Error loading ingredients: " + e.getMessage());
        }
        return ingredients;
    }

    @Override
    public List<FoodItem> loadFoods() {
        List<FoodItem> foods = new ArrayList<>();
        
        // First, load all food names and groups
        String foodQuery = "SELECT f.FoodID, f.FoodDescription, f.FoodCode, g.FoodGroupName FROM food_name f LEFT JOIN food_group g ON f.FoodGroupID = g.FoodGroupID";
        
        // Load all calories in one query - use specific nutrient ID for calories
        String caloriesQuery = "SELECT na.FoodID, nn.NutrientName, nn.NutrientUnit, na.NutrientValue " +
                              "FROM nutrient_amount na " +
                              "JOIN nutrient_name nn ON na.NutrientID = nn.NutrientID " +
                              "WHERE na.NutrientID = 208"; // 208 is the standard nutrient ID for KCAL
        
        // Load all nutrients in one query
        String nutrientsQuery = "SELECT na.FoodID, nn.NutrientName, nn.NutrientUnit, na.NutrientValue " +
                               "FROM nutrient_amount na " +
                               "JOIN nutrient_name nn ON na.NutrientID = nn.NutrientID " +
                               "WHERE nn.NutrientName NOT LIKE '%calor%' AND nn.NutrientName NOT LIKE '%kcal%' AND nn.NutrientName NOT LIKE '%energy%' AND nn.NutrientName NOT LIKE '%kilojoule%'";
        
        try {
            // Load all calories into a map
            Map<Integer, Double> caloriesMap = new HashMap<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(caloriesQuery)) {
                while (rs.next()) {
                    int foodId = rs.getInt("FoodID");
                    String nutrientName = rs.getString("NutrientName");
                    String unit = rs.getString("NutrientUnit");
                    double value = rs.getDouble("NutrientValue");
                    
                    // Convert to calories if needed
                    double calories = value;
                    if (unit != null && (unit.toLowerCase().contains("kj") || unit.toLowerCase().contains("joule"))) {
                        if (unit.toLowerCase().contains("kj")) {
                            calories = value / 4.184; // kJ to kcal
                        } else {
                            calories = value / 4184; // J to kcal
                        }
                    }
                    
                    // Use the first calorie value found for each food
                    if (!caloriesMap.containsKey(foodId)) {
                        caloriesMap.put(foodId, calories);
                    }
                }
            }
            
            // Load all nutrients into a map
            Map<Integer, Map<String, Double>> nutrientsMap = new HashMap<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(nutrientsQuery)) {
                while (rs.next()) {
                    int foodId = rs.getInt("FoodID");
                    String nutrientName = rs.getString("NutrientName");
                    double value = rs.getDouble("NutrientValue");
                    
                    nutrientsMap.computeIfAbsent(foodId, k -> new HashMap<>()).put(nutrientName, value);
                }
            }
            
            // Now load food items with pre-loaded data
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(foodQuery)) {
                
                while (rs.next()) {
                    int foodId = rs.getInt("FoodID");
                    String name = rs.getString("FoodDescription");
                    String foodGroup = rs.getString("FoodGroupName");
                    
                    // Handle null values
                    if (name == null) name = "Unknown Food";
                    if (foodGroup == null) foodGroup = "Unknown Group";
                    
                    // Get pre-loaded calories
                    double calories = caloriesMap.getOrDefault(foodId, 0.0);
                    
                    // Get pre-loaded nutrients
                    Map<String, Double> nutrients = nutrientsMap.getOrDefault(foodId, new HashMap<>());
                    
                    FoodItem food = new FoodItem(foodId, name, calories, nutrients, foodGroup);
                    foods.add(food);
                }
            }
            

            
        } catch (SQLException e) {
            System.err.println("Error loading foods: " + e.getMessage());
        }
        
        return foods;
    }
    
    private double getCaloriesForFood(int foodId) {

        
        // Try multiple possible calorie nutrient IDs
        int[] calorieNutrientIds = {208, 1008}; // 208 is KCAL, 1008 might be another calorie ID
        
        for (int nutrientId : calorieNutrientIds) {
            String query = "SELECT na.NutrientValue, nn.NutrientName, nn.NutrientUnit FROM nutrient_amount na " +
                          "JOIN nutrient_name nn ON na.NutrientID = nn.NutrientID " +
                          "WHERE na.FoodID = ? AND na.NutrientID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, foodId);
                stmt.setInt(2, nutrientId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double calories = rs.getDouble("NutrientValue");
                    String nutrientName = rs.getString("NutrientName");
                    String unit = rs.getString("NutrientUnit");
                    if (calories > 0) {
                        return calories;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error getting calories for food " + foodId + " with nutrient ID " + nutrientId + ": " + e.getMessage());
            }
        }
        
        // If no calories found, try to find any energy-related nutrient
        String fallbackQuery = "SELECT na.NutrientValue, nn.NutrientName, nn.NutrientUnit FROM nutrient_amount na " +
                              "JOIN nutrient_name nn ON na.NutrientID = nn.NutrientID " +
                              "WHERE na.FoodID = ? AND (nn.NutrientName LIKE '%calor%' OR nn.NutrientName LIKE '%kcal%' OR nn.NutrientName LIKE '%energy%' OR nn.NutrientName LIKE '%kilojoule%') " +
                              "AND na.NutrientValue > 0 LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(fallbackQuery)) {
            stmt.setInt(1, foodId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double energyValue = rs.getDouble("NutrientValue");
                String nutrientName = rs.getString("NutrientName");
                String unit = rs.getString("NutrientUnit");
                
                // Convert energy units to calories
                double calories;
                if (nutrientName.toLowerCase().contains("kilojoule") || nutrientName.toLowerCase().contains("kj") || 
                    (unit != null && unit.toLowerCase().contains("kj"))) {
                    // Convert kilojoules to calories (1 kcal = 4.184 kJ)
                    calories = energyValue / 4.184;
                } else if (nutrientName.toLowerCase().contains("joule") || nutrientName.toLowerCase().contains("j") ||
                          (unit != null && unit.toLowerCase().contains("j"))) {
                    // Convert joules to calories (1 kcal = 4184 J)
                    calories = energyValue / 4184.0;
                } else {
                    // Assume it's already in calories
                    calories = energyValue;
                }
                
                return calories;
            }
        } catch (SQLException e) {
            System.err.println("Error in fallback calorie search for food " + foodId + ": " + e.getMessage());
        }
        
        // If still no calories found, return a default value
        return 100.0; // Default calories instead of 0
    }
    
    private Map<String, Double> getNutrientsForFood(int foodId) {
        Map<String, Double> nutrients = new HashMap<>();
        String query = "SELECT nn.NutrientName, na.NutrientValue FROM nutrient_amount na " +
                      "JOIN nutrient_name nn ON na.NutrientID = nn.NutrientID " +
                      "WHERE na.FoodID = ? AND na.NutrientValue > 0";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, foodId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String nutrientName = rs.getString("NutrientName");
                double value = rs.getDouble("NutrientValue");
                nutrients.put(nutrientName, value);
            }
        } catch (SQLException e) {
            System.err.println("Error getting nutrients for food " + foodId + ": " + e.getMessage());
        }
        return nutrients;
    }

    @Override
    public List<Nutrient> loadNutrients() {
        List<Nutrient> nutrients = new ArrayList<>();
        String query = "SELECT na.FoodID, nn.NutrientName, nn.NutrientUnit, na.NutrientValue, ns.NutrientSourceDescription FROM nutrient_amount na LEFT JOIN nutrient_name nn ON na.NutrientID = nn.NutrientID LEFT JOIN nutrient_source ns ON na.NutrientSourceID = ns.NutrientSourceID";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int foodId = rs.getInt("FoodID");
                String name = rs.getString("NutrientName");
                String unit = rs.getString("NutrientUnit");
                double value = rs.getDouble("NutrientValue");
                Nutrient nutrient = new Nutrient(foodId, name, unit, value);
                nutrients.add(nutrient);
            }
        } catch (SQLException e) {
            System.err.println("Error loading nutrients from database: " + e.getMessage());
        }
        return nutrients;
    }
    
    /**
     * Get the maximum user ID from the database
     * This allows dynamic detection of user ID range
     */
    public int getMaxUserId() {
        try {
            // Create a new connection for this operation
            Connection conn = connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for getting max user ID");
                return 20; // Fallback default
            }
            
            try {
                String query = "SELECT MAX(UserID) as max_user_id FROM user_profile";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int maxUserId = rs.getInt("max_user_id");
                        System.out.println("DEBUG: Found max user ID: " + maxUserId);
                        return maxUserId > 0 ? maxUserId : 20; // Return at least 20 if no users found
                    }
                }
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting max user ID: " + e.getMessage());
        }
        
        return 20; // Fallback default
    }
}

