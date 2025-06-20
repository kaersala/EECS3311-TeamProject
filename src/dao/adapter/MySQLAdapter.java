package dao.adapter;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLAdapter implements DatabaseAdapter {
    private Connection connection;

    @Override
    public void connect() {
        String url = ""; // enter your own url, e.g. jdbc:mysql://localhost:3306/cnf2015
        String user = ""; // enter your own user name
        String password = ""; // enter your own password

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("MySQL connection established.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to MySQL database.");
            System.err.println("Reason: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
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
            mealStmt.setDate(2, Date.valueOf(meal.getDate()));
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
            e.printStackTrace();
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
                LocalDate date = mealRs.getDate("Date").toLocalDate();
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
            e.printStackTrace();
        }
    
        return meals;
    }

    @Override
    public void saveProfile(UserProfile profile) {
        String insertProfileQuery = "INSERT INTO user_profile (UserID, UserName, Age, Gender) VALUES (?, ?, ?, ?)";
    
        try (PreparedStatement stmt = connection.prepareStatement(insertProfileQuery)) {
            stmt.setInt(1, profile.getUserID());
            stmt.setString(2, profile.getUserName());
            stmt.setInt(3, profile.getAge());
            stmt.setString(4, profile.getGender());
            stmt.executeUpdate();
    
            String deleteGoalsQuery = "DELETE FROM goal WHERE UserID = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteGoalsQuery)) {
                deleteStmt.setInt(1, profile.getUserID());
                deleteStmt.executeUpdate();
            }
    
            String insertGoalQuery = "INSERT INTO goal (UserID, Nutrient, Direction, Amount, Intensity) VALUES (?, ?, ?, ?, ?)";
            for (Goal goal : profile.getGoals()) {
                try (PreparedStatement stmtGoal = connection.prepareStatement(insertGoalQuery)) {
                    stmtGoal.setInt(1, profile.getUserID());
                    stmtGoal.setString(2, goal.getNutrient());
                    stmtGoal.setString(3, goal.getDirection());
                    stmtGoal.setDouble(4, goal.getAmount());
                    stmtGoal.setString(5, goal.getIntensity());
                    stmtGoal.executeUpdate();
                }
            }
    
        } catch (SQLException e) {
            System.err.println("Error saving profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<UserProfile> loadProfiles() {
        List<UserProfile> profiles = new ArrayList<>();
    
        String profileQuery = "SELECT * FROM user_profile";
        String goalQuery = "SELECT * FROM goal WHERE UserID = ?";
    
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(profileQuery)) {
    
            while (rs.next()) {
                int userID = rs.getInt("UserID");
                String name = rs.getString("UserName");
                int age = rs.getInt("Age");
                String gender = rs.getString("Gender");
    
                List<Goal> goals = new ArrayList<>();
    
                try (PreparedStatement goalStmt = connection.prepareStatement(goalQuery)) {
                    goalStmt.setInt(1, userID);
                    ResultSet goalRS = goalStmt.executeQuery();
                    while (goalRS.next()) {
                        String nutrient = goalRS.getString("Nutrient");
                        String direction = goalRS.getString("Direction");
                        double amount = goalRS.getDouble("Amount");
                        String intensity = goalRS.getString("Intensity");
    
                        Goal goal = new Goal(nutrient, direction, amount, intensity);
                        goals.add(goal);
                    }
                }
    
                UserProfile profile = new UserProfile(userID, name, age, gender, goals);
                profiles.add(profile);
            }
    
        } catch (SQLException e) {
            System.err.println("Error loading profiles: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
        }
    
        return ingredients;
    }

    @Override
    public List<FoodItem> loadFoods() {
        List<FoodItem> foods = new ArrayList<>();
        String query = """
            SELECT f.FoodID, f.FoodCode, f.FoodDescription,
                   g.FoodGroupName, s.FoodSourceDescription
            FROM food_name f
            LEFT JOIN food_group g ON f.FoodGroupID = g.FoodGroupID
            LEFT JOIN food_source s ON f.FoodSourceID = s.FoodSourceID
        """;
    
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
    
            while (rs.next()) {
                int foodId = rs.getInt("FoodID");
                int foodCode = rs.getInt("FoodCode");
                String description = rs.getString("FoodDescription");
                String groupName = rs.getString("FoodGroupName");
                String sourceDescription = rs.getString("FoodSourceDescription");
    
                FoodItem food = new FoodItem(foodId, foodCode, description, groupName, sourceDescription);
                foods.add(food);
            }
    
        } catch (SQLException e) {
            System.err.println("Error loading foods: " + e.getMessage());
            e.printStackTrace();
        }
    
        return foods;
    }

    @Override
    public List<Nutrient> loadNutrients() {
        List<Nutrient> nutrients = new ArrayList<>();
    
        String query = """
            SELECT 
                na.FoodID,
                nn.NutrientName,
                nn.NutrientUnit,
                na.NutrientValue,
                ns.NutrientSourceDescription
            FROM 
                nutrient_amount na
            LEFT JOIN nutrient_name nn ON na.NutrientID = nn.NutrientID
            LEFT JOIN nutrient_source ns ON na.NutrientSourceID = ns.NutrientSourceID
            """;
    
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
    
            while (rs.next()) {
                int foodId = rs.getInt("FoodID");
                String name = rs.getString("NutrientName");
                String unit = rs.getString("NutrientUnit");
                double value = rs.getDouble("NutrientValue");
                String source = rs.getString("NutrientSourceDescription");
    
                Nutrient nutrient = new Nutrient(foodId, name, unit, value, source);
                nutrients.add(nutrient);
            }
    
        } catch (SQLException e) {
            System.err.println("Error loading nutrients from database.");
            e.printStackTrace();
        }
    
        return nutrients;
    }
}
