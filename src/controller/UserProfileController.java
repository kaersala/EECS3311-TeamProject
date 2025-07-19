package controller;

import model.user.UserProfile;
import service.UserProfileManager;
import dao.UserProfileDAO;
import dao.Implementations.UserProfileDAOImpl;

import java.time.LocalDate;
import java.util.List;

public class UserProfileController {
    private final UserProfileManager manager = UserProfileManager.getInstance();
    private final UserProfileDAO userProfileDAO = new UserProfileDAOImpl();

    public void createProfile(String name, String sex,
                              LocalDate dob, double height, double weight) {
        UserProfile newProfile = new UserProfile(name, sex, dob, height, weight);
        newProfile.getSettings().setUnits("metric");
        manager.addProfile(newProfile);
        userProfileDAO.saveUserProfile(newProfile);
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

    public UserProfile getCurrentProfile(){
        return manager.getCurrentProfile();
    }

    public List<UserProfile> getAllProfiles(){
        return userProfileDAO.getAllUserProfiles();
    }

    public void setCurrentProfile(int id){
        manager.setCurrentProfile(id);
    }

    public void updateSettings(String units) {
        UserProfile profile = manager.getCurrentProfile();
        profile.getSettings().setUnits(units);
        userProfileDAO.updateUserProfile(profile);
    }

    public String getUserSettings(){
        return manager.getCurrentProfile().getSettings().getUnits();
    }
} 
