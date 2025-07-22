package service;

import dao.adapter.DatabaseAdapter;
import dao.adapter.MySQLAdapter;
import model.user.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to manage user profiles.
 */
public class UserProfileManager {
    private static UserProfileManager instance;
    private ArrayList<UserProfile> profiles;
    private final DatabaseAdapter db;
    private UserProfile currentProfile;

    private UserProfileManager() {
        db = new MySQLAdapter(); // or swap in a different adapter like CSV/JSON
        db.connect();
        profiles = new ArrayList<>();
        loadProfiles();
    }

    public static UserProfileManager getInstance() {
        if (instance == null) {
            instance = new UserProfileManager();
        }
        return instance;
    }

    public void addProfile(UserProfile p) {
        db.saveProfile(p);
        profiles.add(p);
        if (currentProfile == null) {
            currentProfile = p;
        }
    }

    public UserProfile getProfile(int id) {
        for (UserProfile p : profiles) {
            if (p.getUserID() == id) return p;
        }
        return null;
    }

    public void loadProfiles() {
        List<UserProfile> loaded = db.loadProfiles();
        profiles = new ArrayList<>(loaded);
        if (!profiles.isEmpty()) {
            currentProfile = profiles.get(0); // default selection
        }
    }

    public ArrayList<UserProfile> getProfiles() {
        return profiles;
    }

    public void saveProfile(UserProfile p) {
        db.saveProfile(p);
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getUserID() == p.getUserID()) {
                profiles.set(i, p); // update local list
                break;
            }
        }
    }

    public void setCurrentProfile(int id) {
        for (UserProfile p : profiles) {
            if (p.getUserID() == id) {
                currentProfile = p;
                break;
            }
        }
    }

    public UserProfile getCurrentProfile() {
        return currentProfile;
    }
    
    public void setProfilesForTest(ArrayList<UserProfile> profiles) {
        this.profiles = profiles;
        if (!profiles.isEmpty()) {
            this.currentProfile = profiles.get(0);
        }
    }
}
