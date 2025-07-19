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
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("MySQL connection established.");
            return connection;
        } catch (SQLException e) {
            System.err.println("Failed to connect to MySQL database: " + e.getMessage());
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
                String name = rs.getString("UserName");
                String sex = rs.getString("Sex");
                java.time.LocalDate dob = rs.getDate("Dob").toLocalDate();
                double height = rs.getDouble("Height");
                double weight = rs.getDouble("Weight");
                UserProfile profile = new UserProfile(name, sex, dob, height, weight);
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
        String query = "SELECT FoodID, FoodDescription, FoodCode, FoodGroupName, FoodSourceDescription, Calories FROM food_name LEFT JOIN food_group ON food_name.FoodGroupID = food_group.FoodGroupID LEFT JOIN food_source ON food_name.FoodSourceID = food_source.FoodSourceID";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int foodId = rs.getInt("FoodID");
                String name = rs.getString("FoodDescription");
                double calories = rs.getDouble("Calories");
                String foodGroup = rs.getString("FoodGroupName");
                // nutrients field needs to be queried separately or supplemented later
                Map<String, Double> nutrients = new HashMap<>();
                FoodItem food = new FoodItem(foodId, name, calories, nutrients, foodGroup);
                foods.add(food);
            }
        } catch (SQLException e) {
            System.err.println("Error loading foods: " + e.getMessage());
        }
        return foods;
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

