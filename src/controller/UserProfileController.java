package controller;

import model.user.UserProfile;
import service.UserProfileManager;

import java.time.LocalDate;
import java.util.List;

public class UserProfileController {
    private final UserProfileManager manager = UserProfileManager.getInstance();
    public void createProfile(String name, String sex,
                              LocalDate dob, double height, double weight) {
        UserProfile newProfile = new UserProfile(name, sex, dob, height, weight);
        newProfile.getSettings().setUnits("metric");
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

        manager.saveProfile(profile);
    }

    public UserProfile getCurrentProfile(){
        return manager.getCurrentProfile();
    }

    public List<UserProfile> getAllProfiles(){
        return manager.loadProfiles();
    }

    public void setProfile(int id){
        manager.setCurrentProfile(id);
    }
    public void updateSettings(String units) {
        UserProfile profile = manager.getCurrentProfile();
        profile.getSettings().setUnits(units);
        manager.saveProfile(profile); // Or however persistence is handled
    }
}
