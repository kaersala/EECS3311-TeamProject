package dao.implementations;

import adapter.MySQLAdapter;
import dao.interfaces.IUserProfileDAO;
import model.user.UserProfile;
import model.user.Goal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserProfileDAO implements IUserProfileDAO {
    private final MySQLAdapter adapter;

    public UserProfileDAO(MySQLAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public UserProfile getUserProfile(int userId) {
        String sql = "SELECT * FROM user_profile WHERE userID = ?";
        try (Connection conn = adapter.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                Goal goal = Goal.valueOf(rs.getString("goal"));
                return new UserProfile(userId, name, goal);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // TODO: Replace with proper logging
        }
        return null;
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        List<UserProfile> profiles = new ArrayList<>();
        String sql = "SELECT * FROM user_profile";
        try (Connection conn = adapter.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int userId = rs.getInt("userID");
                String name = rs.getString("name");
                Goal goal = Goal.valueOf(rs.getString("goal"));
                profiles.add(new UserProfile(userId, name, goal));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // TODO: Replace with proper logging
        }
        return profiles;
    }

    @Override
    public void saveUserProfile(UserProfile profile) {
        String sql = "INSERT INTO user_profile (userID, name, goal) VALUES (?, ?, ?)";
        try (Connection conn = adapter.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profile.getUserID());
            stmt.setString(2, profile.getName());
            stmt.setString(3, profile.getGoal().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // TODO: Replace with proper logging
        }
    }

    @Override
    public void updateUserProfile(UserProfile profile) {
        String sql = "UPDATE user_profile SET name = ?, goal = ? WHERE userID = ?";
        try (Connection conn = adapter.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, profile.getName());
            stmt.setString(2, profile.getGoal().name());
            stmt.setInt(3, profile.getUserID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // TODO: Replace with proper logging
        }
    }

    @Override
    public void deleteUserProfile(int userId) {
        String sql = "DELETE FROM user_profile WHERE userID = ?";
        try (Connection conn = adapter.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // TODO: Replace with proper logging
        }
    }
}
