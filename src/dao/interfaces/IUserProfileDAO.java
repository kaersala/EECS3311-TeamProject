package dao.interfaces;

import model.user.UserProfile;
import java.util.List;

public interface IUserProfileDAO {
    UserProfile getUserProfile(int userId);
    List<UserProfile> getAllUserProfiles();
    void saveUserProfile(UserProfile profile);
    void updateUserProfile(UserProfile profile);
    void deleteUserProfile(int userId);
}
