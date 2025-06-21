package dao.Implementations;

import dao.interfaces.IMealDAO;
import model.meal.Meal;
import adapter.MySQLAdapter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MealDAO implements IMealDAO {
    private final MySQLAdapter adapter;

    public MealDAO(MySQLAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public List<Meal> getMealsByUserId(int userId) {
        List<Meal> meals = new ArrayList<>();
        String query = "SELECT * FROM meal WHERE UserID = ?";

        try (Connection conn = adapter.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Meal meal = new Meal(
                    rs.getInt("MealID"),
                    rs.getInt("UserID"),
                    rs.getString("Name"),
                    rs.getDate("Date").toLocalDate(),
                    rs.getInt("Calories")
                );
                meals.add(meal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return meals;
    }

    @Override
    public Meal getMealById(int mealId) {
        String query = "SELECT * FROM meal WHERE MealID = ?";
        try (Connection conn = adapter.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, mealId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Meal(
                    rs.getInt("MealID"),
                    rs.getInt("UserID"),
                    rs.getString("Name"),
                    rs.getDate("Date").toLocalDate(),
                    rs.getInt("Calories")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void saveMeal(Meal meal) {
        String query = "INSERT INTO meal (UserID, Name, Date, Calories) VALUES (?, ?, ?, ?)";

        try (Connection conn = adapter.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, meal.getUserId());
            stmt.setString(2, meal.getName());
            stmt.setDate(3, Date.valueOf(meal.getDate()));
            stmt.setInt(4, meal.getCalories());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
