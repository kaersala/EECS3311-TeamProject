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
        // Use configuration class to get database connection information
        String url = DatabaseConfig.getDatabaseUrl();
        String user = DatabaseConfig.getUsername();
        String password = DatabaseConfig.getPassword();
        
        // Print configuration information (for debugging)
        DatabaseConfig.printConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);

            //  Add this to confirm success
            System.out.println("CONNECTED: " + (connection != null));
            return connection;
        } catch (Exception e) {
            System.err.println(" DB CONNECTION ERROR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    

    @Override
    public void saveMeal(Meal meal) {
        String mealInsert = "INSERT INTO meal (UserID, Date, Type) VALUES (?, ?, ?)";
        String ingredientInsert = "INSERT INTO ingredient (MealID, FoodID, Quantity) VALUES (?, ?, ?)";
        try (
                PreparedStatement mealStmt = connection.prepareStatement(mealInsert, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement ingredientStmt = connection.prepareStatement(ingredientInsert)
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
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving meal: " + e.getMessage());
        }
    }

    @Override
    public void updateMeal(Meal meal) {
        String mealUpdate = "UPDATE meal SET UserID = ?, Date = ?, Type = ? WHERE MealID = ?";
        String deleteIngredients = "DELETE FROM ingredient WHERE MealID = ?";
        String ingredientInsert = "INSERT INTO ingredient (MealID, FoodID, Quantity) VALUES (?, ?, ?)";
        
        try (
                PreparedStatement mealStmt = connection.prepareStatement(mealUpdate);
                PreparedStatement deleteStmt = connection.prepareStatement(deleteIngredients);
                PreparedStatement ingredientStmt = connection.prepareStatement(ingredientInsert)
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
            
        } catch (SQLException e) {
            System.err.println("Error updating meal: " + e.getMessage());
        }
    }

    @Override
    public List<Meal> loadMeals(int userId) {
        List<Meal> meals = new ArrayList<>();
        String mealQuery = "SELECT * FROM meal WHERE UserID = ?";
        String ingredientQuery = "SELECT FoodID, Quantity FROM ingredient WHERE MealID = ?";
        try (
                PreparedStatement mealStmt = connection.prepareStatement(mealQuery);
                PreparedStatement ingredientStmt = connection.prepareStatement(ingredientQuery)
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
                    ingredients.add(new IngredientEntry(foodId, quantity));
                }
                meals.add(new Meal(mealId, userId, date, type, ingredients));
            }
        } catch (SQLException e) {
            System.err.println("Error loading meals: " + e.getMessage());
        }
        return meals;
    }

    @Override
    public void saveProfile(UserProfile profile) {
        // Simple validation: Reject if name is null or empty
        if (profile.getName() == null || profile.getName().trim().isEmpty()) {
            System.err.println("Invalid profile: name is null or empty.");
            return; // Do not proceed with DB insert
        }

        String insertProfileQuery = "INSERT INTO user_profile (UserID, UserName, Sex, Dob, Height, Weight) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertProfileQuery)) {
            stmt.setInt(1, profile.getUserID());
            stmt.setString(2, profile.getName());
            stmt.setString(3, profile.getSex());
            stmt.setDate(4, java.sql.Date.valueOf(profile.getDob()));
            stmt.setDouble(5, profile.getHeight());
            stmt.setDouble(6, profile.getWeight());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving profile: " + e.getMessage());
        }
    }

    @Override
    public List<UserProfile> loadProfiles() {
        List<UserProfile> profiles = new ArrayList<>();
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
        String query = "SELECT f.FoodID, f.FoodDescription, f.FoodCode, g.FoodGroupName, s.FoodSourceDescription FROM food_name f LEFT JOIN food_group g ON f.FoodGroupID = g.FoodGroupID LEFT JOIN food_source s ON f.FoodSourceID = s.FoodSourceID";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int foodId = rs.getInt("FoodID");
                String name = rs.getString("FoodDescription");
                String foodGroup = rs.getString("FoodGroupName");
                
                // Get calories for this food (NutrientID = 208 for KCAL)
                double calories = getCaloriesForFood(foodId);
                
                // Get nutrients for this food
                Map<String, Double> nutrients = getNutrientsForFood(foodId);
                
                FoodItem food = new FoodItem(foodId, name, calories, nutrients, foodGroup);
                foods.add(food);
            }
        } catch (SQLException e) {
            System.err.println("Error loading foods: " + e.getMessage());
        }
        return foods;
    }
    
    private double getCaloriesForFood(int foodId) {
        String query = "SELECT NutrientValue FROM nutrient_amount WHERE FoodID = ? AND NutrientID = 208";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, foodId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("NutrientValue");
            }
        } catch (SQLException e) {
            System.err.println("Error getting calories for food " + foodId + ": " + e.getMessage());
        }
        return 0.0; // Default calories if not found
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
}
