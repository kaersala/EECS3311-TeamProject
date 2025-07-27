package dao.Implementations;

import dao.interfaces.IGoalDAO;
import model.Goal;
import dao.adapter.DatabaseAdapter;
import dao.adapter.MySQLAdapter;
import dao.adapter.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of IGoalDAO for database operations
 */
public class GoalDAO implements IGoalDAO {
    
    private DatabaseAdapter databaseAdapter;
    
    public GoalDAO() {
        this.databaseAdapter = DatabaseManager.getAdapter();
    }
    
    public GoalDAO(DatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
    }
    
    @Override
    public void saveGoals(int userId, List<Goal> goals) {
        // First delete existing goals for this user
        deleteGoals(userId);
        
        // Then insert new goals
        String insertQuery = "INSERT INTO user_goals (UserID, Nutrient, Direction, Amount, Intensity, CreatedAt) VALUES (?, ?, ?, ?, ?, NOW())";
        
        try {
            Connection conn = databaseAdapter.connect();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    for (Goal goal : goals) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, goal.getNutrient());
                        stmt.setString(3, goal.getDirection());
                        stmt.setDouble(4, goal.getAmount());
                        stmt.setString(5, goal.getIntensity());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                    System.out.println("Saved " + goals.size() + " goals for user " + userId);
                } finally {
                    // Always close the connection
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                }
            } else {
                throw new SQLException("Database connection is null");
            }
        } catch (SQLException e) {
            System.err.println("Error saving goals: " + e.getMessage());
            // Fallback to in-memory storage for demo
            saveGoalsInMemory(userId, goals);
        }
    }
    
    @Override
    public List<Goal> loadGoals(int userId) {
        List<Goal> goals = new ArrayList<>();
        String query = "SELECT Nutrient, Direction, Amount, Intensity FROM user_goals WHERE UserID = ?";
        
        try {
            Connection conn = databaseAdapter.connect();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        String nutrient = rs.getString("Nutrient");
                        String direction = rs.getString("Direction");
                        double amount = rs.getDouble("Amount");
                        String intensity = rs.getString("Intensity");
                        
                        if (nutrient != null && direction != null) {
                            Goal goal = new Goal(nutrient, direction, amount, intensity);
                            goals.add(goal);
                        }
                    }
                } finally {
                    // Always close the connection
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                }
            } else {
                throw new SQLException("Database connection is null");
            }
        } catch (SQLException e) {
            System.err.println("Error loading goals: " + e.getMessage());
            // Fallback to in-memory storage for demo
            return loadGoalsFromMemory(userId);
        }
        
        return goals;
    }
    
    @Override
    public void updateGoals(int userId, List<Goal> goals) {
        saveGoals(userId, goals); // For simplicity, delete and re-insert
    }
    
    @Override
    public void deleteGoals(int userId) {
        String deleteQuery = "DELETE FROM user_goals WHERE UserID = ?";
        
        try {
            Connection conn = databaseAdapter.connect();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, userId);
                    int rowsAffected = stmt.executeUpdate();
                    System.out.println("Deleted " + rowsAffected + " goals for user " + userId);
                } finally {
                    // Always close the connection
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                }
            } else {
                throw new SQLException("Database connection is null");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting goals: " + e.getMessage());
            // Fallback to in-memory storage for demo
            deleteGoalsFromMemory(userId);
        }
    }
    
    @Override
    public boolean hasGoals(int userId) {
        String countQuery = "SELECT COUNT(*) FROM user_goals WHERE UserID = ?";
        
        try {
            Connection conn = databaseAdapter.connect();
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(countQuery)) {
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                } finally {
                    // Always close the connection
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                }
            } else {
                throw new SQLException("Database connection is null");
            }
        } catch (SQLException e) {
            System.err.println("Error checking goals: " + e.getMessage());
            // Fallback to in-memory storage for demo
            return hasGoalsInMemory(userId);
        }
        
        return false;
    }
    
    // In-memory fallback methods for demo purposes
    private static final java.util.Map<Integer, List<Goal>> inMemoryGoals = new java.util.HashMap<>();
    
    private void saveGoalsInMemory(int userId, List<Goal> goals) {
        inMemoryGoals.put(userId, new ArrayList<>(goals));
        System.out.println("Goals saved in memory for user " + userId);
    }
    
    private List<Goal> loadGoalsFromMemory(int userId) {
        return inMemoryGoals.getOrDefault(userId, new ArrayList<>());
    }
    
    private void deleteGoalsFromMemory(int userId) {
        inMemoryGoals.remove(userId);
    }
    
    private boolean hasGoalsInMemory(int userId) {
        return inMemoryGoals.containsKey(userId) && !inMemoryGoals.get(userId).isEmpty();
    }
    
    /**
     * Calculate default amount based on nutrient and activity level
     */
    private double calculateDefaultAmount(String nutrient, String activityLevel) {
        // Base amounts for different nutrients
        double baseAmount = switch (nutrient.toLowerCase()) {
            case "fiber" -> 5.0;      // 5g base
            case "calories" -> 200.0;  // 200 calories base
            case "protein" -> 10.0;    // 10g base
            case "fat" -> 15.0;        // 15g base
            case "carbohydrates" -> 25.0; // 25g base
            case "sodium" -> 500.0;    // 500mg base
            default -> 5.0;
        };
        
        // Adjust based on activity level
        if (activityLevel != null) {
            return switch (activityLevel.toLowerCase()) {
                case "low" -> baseAmount * 0.5;
                case "moderate" -> baseAmount;
                case "high" -> baseAmount * 1.5;
                default -> baseAmount;
            };
        }
        
        return baseAmount;
    }
} 