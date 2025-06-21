package dao.implementation;

import dao.interfaces.IIngredientDAO;
import model.IngredientEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO implements IIngredientDAO {
    private Connection connection;

    public IngredientDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<IngredientEntry> loadIngredients() {
        List<IngredientEntry> ingredients = new ArrayList<>();
        String query = "SELECT FoodID, Quantity FROM ingredient_entry";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int foodId = rs.getInt("FoodID");
                double quantity = rs.getDouble("Quantity");

                IngredientEntry entry = new IngredientEntry(foodId, quantity);
                ingredients.add(entry);
            }

        } catch (SQLException e) {
            System.err.println("Error loading ingredients: " + e.getMessage());
            e.printStackTrace();
        }

        return ingredients;
    }

    @Override
    public IngredientEntry getIngredientByFoodId(int foodId) {
        String query = "SELECT FoodID, Quantity FROM ingredient_entry WHERE FoodID = ?";
        IngredientEntry entry = null;

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, foodId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double quantity = rs.getDouble("Quantity");
                entry = new IngredientEntry(foodId, quantity);
            }

        } catch (SQLException e) {
            System.err.println("Error getting ingredient by FoodID: " + e.getMessage());
            e.printStackTrace();
        }

        return entry;
    }
}
