package dao.implementations;

import adapter.JsonAdapter;
import dao.interfaces.IUserProfileDAO;
import model.user.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class UserProfileDAO implements IUserProfileDAO {

    private final JsonAdapter adapter;

    public UserProfileDAO() {
        this.adapter = new JsonAdapter();
    }

    @Override
    public UserProfile getUserProfile(int userId) {
        UserProfile profile = adapter.loadUserProfile();
        return (profile != null && profile.getUserID() == userId) ? profile : null;
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        List<UserProfile> result = new ArrayList<>();
        UserProfile profile = adapter.loadUserProfile();
        if (profile != null) result.add(profile);
        return result;
    }

    @Override
    public void saveUserProfile(UserProfile profile) {
        adapter.saveUserProfile(profile);
    }

    @Override
    public void updateUserProfile(UserProfile profile) {
        adapter.saveUserProfile(profile);
    }

    @Override
    public void deleteUserProfile(int userId) {
        java.io.File file = new java.io.File("data/json/profile.json");
        if (file.exists()) file.delete();
    }
}
