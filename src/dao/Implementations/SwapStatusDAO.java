package dao.Implementations;

import dao.adapter.DatabaseAdapter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SwapStatusDAO {
    private DatabaseAdapter databaseAdapter;
    
    public SwapStatusDAO(DatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
    }
    
    /**
     * Mark a meal as swapped and store original data
     */
    public boolean markMealAsSwapped(int userId, int mealId, LocalDate date, String originalMealData) {
        String sql = "INSERT INTO swap_status (user_id, meal_id, date, is_swapped, original_meal_data) " +
                    "VALUES (?, ?, ?, TRUE, ?) " +
                    "ON DUPLICATE KEY UPDATE is_swapped = TRUE, original_meal_data = ?, swap_timestamp = CURRENT_TIMESTAMP";
        
        try {
            // Create a new connection for this operation
            Connection conn = databaseAdapter.connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for marking meal as swapped");
                return false;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, mealId);
                stmt.setDate(3, Date.valueOf(date));
                stmt.setString(4, originalMealData);
                stmt.setString(5, originalMealData);
                
                int result = stmt.executeUpdate();
                return result > 0;
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error marking meal as swapped: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark a meal as restored (not swapped)
     */
    public boolean markMealAsRestored(int userId, int mealId, LocalDate date) {
        String sql = "UPDATE swap_status SET is_swapped = FALSE WHERE user_id = ? AND meal_id = ? AND date = ?";
        
        try {
            // Create a new connection for this operation
            Connection conn = databaseAdapter.connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for marking meal as restored");
                return false;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, mealId);
                stmt.setDate(3, Date.valueOf(date));
                
                int result = stmt.executeUpdate();
                return result > 0;
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error marking meal as restored: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if any meals for a specific date have been swapped
     */
    public boolean hasMealsBeenSwapped(int userId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM swap_status WHERE user_id = ? AND date = ? AND is_swapped = TRUE";
        
        try {
            // Create a new connection for this operation
            Connection conn = databaseAdapter.connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for swap status check");
                return false;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, Date.valueOf(date));
                
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
            
        } catch (SQLException e) {
            System.err.println("Error checking if meals have been swapped: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if a specific meal has been swapped
     */
    public boolean hasMealBeenSwapped(int userId, int mealId, LocalDate date) {
        String sql = "SELECT is_swapped FROM swap_status WHERE user_id = ? AND meal_id = ? AND date = ?";
        
        try {
            // Create a new connection for this operation
            Connection conn = databaseAdapter.connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for meal swap status check");
                return false;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, mealId);
                stmt.setDate(3, Date.valueOf(date));
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getBoolean("is_swapped");
                }
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking if meal has been swapped: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get original meal data for restoration
     */
    public String getOriginalMealData(int userId, int mealId, LocalDate date) {
        String sql = "SELECT original_meal_data FROM swap_status WHERE user_id = ? AND meal_id = ? AND date = ? AND is_swapped = TRUE";
        
        try {
            // Create a new connection for this operation
            Connection conn = databaseAdapter.connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for getting original meal data");
                return null;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, mealId);
                stmt.setDate(3, Date.valueOf(date));
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("original_meal_data");
                }
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting original meal data: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all swapped meal IDs for a specific date
     */
    public List<Integer> getSwappedMealIds(int userId, LocalDate date) {
        List<Integer> mealIds = new ArrayList<>();
        String sql = "SELECT meal_id FROM swap_status WHERE user_id = ? AND date = ? AND is_swapped = TRUE";
        
        try {
            // Create a new connection for this operation
            Connection conn = databaseAdapter.connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for getting swapped meal IDs");
                return mealIds;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, Date.valueOf(date));
                
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    mealIds.add(rs.getInt("meal_id"));
                }
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting swapped meal IDs: " + e.getMessage());
        }
        
        return mealIds;
    }
    
    /**
     * Delete swap status record by meal ID
     */
    public boolean deleteSwapStatusByMealId(int mealId) {
        String sql = "DELETE FROM swap_status WHERE meal_id = ?";
        
        try {
            // Create a new connection for this operation
            Connection conn = databaseAdapter.connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for deleting swap status");
                return false;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, mealId);
                
                int result = stmt.executeUpdate();
                return result >= 0; // Return true even if no rows were deleted
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting swap status by meal ID: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all swapped meals for a user
     */
    public List<SwapStatusRecord> getSwappedMeals(int userId) {
        String sql = "SELECT meal_id, date, original_meal_data FROM swap_status WHERE user_id = ? AND is_swapped = TRUE ORDER BY date";
        List<SwapStatusRecord> records = new ArrayList<>();
        
        try {
            // Create a new connection for this operation
            Connection conn = databaseAdapter.connect();
            if (conn == null) {
                System.err.println("Failed to create database connection for getting swapped meals");
                return records;
            }
            
            System.out.println("=== SwapStatusDAO Debug ===");
            System.out.println("Querying for user ID: " + userId);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        SwapStatusRecord record = new SwapStatusRecord();
                        record.setMealId(rs.getInt("meal_id"));
                        record.setDate(rs.getDate("date").toLocalDate());
                        record.setOriginalMealData(rs.getString("original_meal_data"));
                        records.add(record);
                        System.out.println("Found swapped meal: ID=" + record.getMealId() + ", Date=" + record.getDate());
                    }
                }
            } finally {
                // Always close the connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
            
            System.out.println("Total swapped meals found: " + records.size());
            
        } catch (SQLException e) {
            System.err.println("Error getting swapped meals: " + e.getMessage());
            e.printStackTrace();
        }
        
        return records;
    }
    
    /**
     * Record class for swap status data
     */
    public static class SwapStatusRecord {
        private int mealId;
        private LocalDate date;
        private String originalMealData;
        
        public int getMealId() { return mealId; }
        public void setMealId(int mealId) { this.mealId = mealId; }
        
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public String getOriginalMealData() { return originalMealData; }
        public void setOriginalMealData(String originalMealData) { this.originalMealData = originalMealData; }
    }
} 