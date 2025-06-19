package service;

import dao.adapter.DatabaseAdapter;
import model.user.Settings;
import model.user.UserProfile;

import java.util.ArrayList;

public class UserProfileManager {
    private static UserProfileManager instance;
    private ArrayList<UserProfile> profiles;
    private DatabaseAdapter db;
    private UserProfile currentProfile;

    public static UserProfileManager getInstance() {
        if (instance == null) instance = new UserProfileManager();
        return instance;
    }

    public void addProfile(UserProfile p) {
        //save to dao
    }

    public void getProfile(int id) {
        // get a profile from dao
    }

    public void loadProfiles() {
        // get profiles from dao and store in profiles
    }

    public ArrayList<UserProfile> getProfiles() {
        return profiles;
    }

    public void saveProfile(UserProfile p) {
        // logic to save/ update a profile
    }

    public void setCurrentProfile(int id) {
        //set current profile to profile with given id
    }

    public UserProfile getCurrentProfile() {
        return currentProfile;
    }


}

