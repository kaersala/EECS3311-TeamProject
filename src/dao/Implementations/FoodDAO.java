package dao.implementations;

import dao.interfaces.IFoodDAO;
import model.user.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                int foodCode = rs.getInt("FoodCode");
                String description = rs.getString("FoodDescription");
                String groupName = rs.getString("FoodGroupName");
                String sourceDescription = rs.getString("FoodSourceDescription");

                FoodItem food = new FoodItem(foodId, foodCode, description, groupName, sourceDescription);
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
                    int foodCode = rs.getInt("FoodCode");
                    String description = rs.getString("FoodDescription");
                    String groupName = rs.getString("FoodGroupName");
                    String sourceDescription = rs.getString("FoodSourceDescription");

                    return new FoodItem(foodId, foodCode, description, groupName, sourceDescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving food by ID: " + e.getMessage());
        }

        return null;
    }
}
