package controller;

import model.user.UserProfile;
import service.UserProfileManager;
import dao.interfaces.IUserProfileDAO;
import dao.Implementations.UserProfileDAO;

import java.time.LocalDate;
import java.util.List;

public class UserProfileController {
    private final UserProfileManager manager = UserProfileManager.getInstance();
    private final IUserProfileDAO userProfileDAO = new UserProfileDAO();

    public void createProfile(String name, String sex,
                              LocalDate dob, double height, double weight) {
        // Check if a profile with this name already exists
        List<UserProfile> existingProfiles = userProfileDAO.getAllUserProfiles();
        boolean nameExists = existingProfiles.stream()
                .anyMatch(profile -> profile.getName().equals(name));
        
        if (nameExists) {
            throw new IllegalArgumentException("A profile with the name '" + name + "' already exists. Please choose a different name.");
        }
        
        UserProfile newProfile = new UserProfile(name, sex, dob, height, weight);
        newProfile.getSettings().setUnits("metric");
        
        // Save to database first, then add to manager
        userProfileDAO.saveUserProfile(newProfile);
        manager.addProfile(newProfile);
    }

    public void editProfile(String name, String sex,
                            LocalDate dob, double height, double weight) {
        UserProfile profile = manager.getCurrentProfile();
        profile.setName(name);
        profile.setSex(sex);
        profile.setDob(dob);
        profile.setHeight(height);
        profile.setWeight(weight);
        userProfileDAO.updateUserProfile(profile);
    }

    public UserProfile getCurrentProfile() {
        return manager.getCurrentProfile();
    }

    public List<UserProfile> getAllProfiles() {
        return userProfileDAO.getAllUserProfiles();
    }

    public void setCurrentProfile(int id) {
        manager.setCurrentProfile(id);
    }

    public void updateSettings(String units) {
        UserProfile profile = manager.getCurrentProfile();
        profile.getSettings().setUnits(units);
        userProfileDAO.updateUserProfile(profile);
    }

    public String getUserSettings() {
    	UserProfile profile = manager.getCurrentProfile();
        return (profile != null && profile.getSettings() != null) 
               ? profile.getSettings().getUnits() 
               : "Metric"; // default fallback
    }

    public void deleteProfile(int userId) {
        userProfileDAO.deleteUserProfile(userId);
        // Remove from manager if it's the current profile
        UserProfile currentProfile = manager.getCurrentProfile();
        if (currentProfile != null && currentProfile.getUserID() == userId) {
            manager.removeCurrentProfile();
        }
    }
}
