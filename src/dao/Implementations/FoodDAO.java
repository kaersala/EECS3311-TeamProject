package dao.Implementations;

import dao.interfaces.IFoodDAO;
import model.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodDAO implements IFoodDAO {

    private final Connection connection;

    public FoodDAO(Connection connection) {
        this.connection = connection;
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
                String name = rs.getString("FoodDescription");
                String groupName = rs.getString("FoodGroupName");
                
                // Get calories for this food (NutrientID = 208 for KCAL)
                double calories = getCaloriesForFood(foodId);
                
                // Get nutrients for this food
                Map<String, Double> nutrients = getNutrientsForFood(foodId);

                FoodItem food = new FoodItem(foodId, name, calories, nutrients, groupName);
                foods.add(food);
            }

        } catch (SQLException e) {
            System.err.println("Error loading foods: " + e.getMessage());
        }

        return foods;
    }

    @Override
    public FoodItem getFoodById(int foodId) {
        String query = """
            SELECT f.FoodID, f.FoodCode, f.FoodDescription,
                   g.FoodGroupName, s.FoodSourceDescription
            FROM food_name f
            LEFT JOIN food_group g ON f.FoodGroupID = g.FoodGroupID
            LEFT JOIN food_source s ON f.FoodSourceID = s.FoodSourceID
            WHERE f.FoodID = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, foodId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("FoodDescription");
                    String groupName = rs.getString("FoodGroupName");
                    
                    // Get calories for this food
                    double calories = getCaloriesForFood(foodId);
                    
                    // Get nutrients for this food
                    Map<String, Double> nutrients = getNutrientsForFood(foodId);

                    return new FoodItem(foodId, name, calories, nutrients, groupName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving food by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get calories for a specific food item
     * NutrientID = 208 represents KCAL (kilocalories)
     */
    private double getCaloriesForFood(int foodId) {
        String query = "SELECT NutrientValue FROM nutrient_amount WHERE FoodID = ? AND NutrientID = 208";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, foodId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("NutrientValue");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting calories for food " + foodId + ": " + e.getMessage());
        }
        return 0.0; // Default value if not found
    }

    /**
     * Get nutrients for a specific food item
     */
    private Map<String, Double> getNutrientsForFood(int foodId) {
        Map<String, Double> nutrients = new HashMap<>();
        String query = """
            SELECT nn.NutrientName, na.NutrientValue
            FROM nutrient_amount na
            JOIN nutrient_name nn ON na.NutrientID = nn.NutrientID
            WHERE na.FoodID = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, foodId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String nutrientName = rs.getString("NutrientName");
                    double value = rs.getDouble("NutrientValue");
                    nutrients.put(nutrientName, value);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting nutrients for food " + foodId + ": " + e.getMessage());
        }
        return nutrients;
    }
}
