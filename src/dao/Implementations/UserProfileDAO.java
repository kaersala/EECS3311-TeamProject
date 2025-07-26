package dao.Implementations;

import dao.adapter.DatabaseAdapter;
import dao.adapter.MySQLAdapter;
import dao.interfaces.IUserProfileDAO;
import model.user.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class UserProfileDAO implements IUserProfileDAO {

    private final DatabaseAdapter adapter;

    public UserProfileDAO() {
        this.adapter = new MySQLAdapter();
        try {
        this.adapter.connect();
        } catch (Exception e) {
            System.err.println("Warning: Could not connect to database: " + e.getMessage());
            // Continue without database connection
        }
    }

    @Override
    public UserProfile getUserProfile(int userId) {
        List<UserProfile> profiles = adapter.loadProfiles();
        return profiles.stream()
                .filter(profile -> profile.getUserID() == userId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        return adapter.loadProfiles();
    }

    @Override
    public void saveUserProfile(UserProfile profile) {
        adapter.saveProfile(profile);
    }

    @Override
    public void updateUserProfile(UserProfile profile) {
        if (adapter instanceof MySQLAdapter) {
            ((MySQLAdapter) adapter).updateProfile(profile);
        } else {
        adapter.saveProfile(profile);
        }
    }

    @Override
    public void deleteUserProfile(int userId) {
        if (adapter instanceof MySQLAdapter) {
            ((MySQLAdapter) adapter).deleteProfile(userId);
        } else {
        System.out.println("Delete user profile functionality needs to be implemented in DatabaseAdapter");
        }
    }
}
