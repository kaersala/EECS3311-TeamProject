package test;

import dao.adapter.MySQLAdapter;
import dao.Implementations.UserProfileDAO;
import model.user.UserProfile;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TC09_InputValidationTest {

    @Test
    public void testInvalidProfileInputValidation() {
        // Step 1: Setup DAO and adapter
        MySQLAdapter adapter = new MySQLAdapter();
        adapter.connect();
        UserProfileDAO profileDAO = new UserProfileDAO();

        // Step 2: Create invalid profile
        UserProfile invalidProfile = new UserProfile(
                "",                         // Invalid: Empty name
                "Other",                    // Assuming "Other" is valid
                LocalDate.now().plusDays(1),// Invalid: Future DOB
                -160.0,                     // Invalid: Negative height
                -55.0                       // Invalid: Negative weight
        );
        invalidProfile.setUserID(9999); // Arbitrary test ID that doesn't collide

        // Step 3: Attempt to save invalid profile
        profileDAO.saveUserProfile(invalidProfile);  // Method should handle validation

        // Step 4: Load profiles to check if invalid one was saved
        boolean profileSaved = profileDAO.getAllUserProfiles().stream()
                .anyMatch(p -> p.getUserID() == 9999);

        // Step 5: Assert profile was not saved
        assertFalse(profileSaved, "Invalid profile should not be saved to database.");
    }
}
