package dao.Implementations;

import dao.adapter.MySQLAdapter;
import dao.interfaces.INutrientDAO;
import model.Nutrient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NutrientDAO implements INutrientDAO {

    private final MySQLAdapter db;

    public NutrientDAO(MySQLAdapter db) {
        this.db = db;
    }

    @Override
    public List<Nutrient> loadAllNutrients() {
        List<Nutrient> nutrients = new ArrayList<>();
        String query = "SELECT * FROM nutrient_name";

        try (Connection conn = db.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("NutrientID");
                String name = rs.getString("NutrientSymbol");
                String unit = rs.getString("NutrientUnit");
                double amount = 0.0; // Default amount, you might want to get this from nutrient_amount table
                nutrients.add(new Nutrient(id, name, unit, amount));
            }

        } catch (SQLException e) {
            System.err.println("Error loading nutrients: " + e.getMessage());
        }

        return nutrients;
    }

    @Override
    public Nutrient getNutrientById(int nutrientId) {
        Nutrient nutrient = null;
        String query = "SELECT * FROM nutrient_name WHERE NutrientID = ?";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, nutrientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("NutrientSymbol");
                String unit = rs.getString("NutrientUnit");
                double amount = 0.0; // Default amount
                nutrient = new Nutrient(nutrientId, name, unit, amount);
            }

        } catch (SQLException e) {
            System.err.println("Error getting nutrient by ID: " + e.getMessage());
        }

        return nutrient;
    }
}
