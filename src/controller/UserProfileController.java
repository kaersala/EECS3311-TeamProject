package controller;

import model.user.UserProfile;
import service.UserProfileManager;

import java.time.LocalDate;
import adapter.JsonAdapter;
import java.util.ArrayList;
import java.util.List;

public class UserProfileController {
    private final UserProfileManager manager = UserProfileManager.getInstance();
    private JsonAdapter adapter = new JsonAdapter();

    public void createProfile(String name, String sex,
                              LocalDate dob, double height, double weight) {
        UserProfile newProfile = new UserProfile(name, sex, dob, height, weight);
        newProfile.getSettings().setUnits("metric");
        manager.addProfile(newProfile);
        List<UserProfile> list = new ArrayList<>();
        list.add(newProfile);
        adapter.saveUserProfiles(list, "userprofile.json");
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
        return manager.getProfiles();
    }

    public void setCurrentProfile(int id){
        manager.setCurrentProfile(id);
    }
    public void updateSettings(String units) {
        UserProfile profile = manager.getCurrentProfile();
        profile.getSettings().setUnits(units);
        manager.saveProfile(profile); // Or however persistence is handled
    }
    public String getUserSettings(){
        return manager.getCurrentProfile().getSettings().getUnits();
    }

    public List<UserProfile> loadAllProfilesFromJson() {
        List<String> jsonList = adapter.loadAllJsonStrings("userprofile");
        List<UserProfile> profiles = new ArrayList<>();
        for (String json : jsonList) {
            UserProfile profile = adapter.deserializeUserProfile(json);
            if (profile != null) {
                profiles.add(profile);
            }
        }
        return profiles;
    }
}
