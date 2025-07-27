package test;

import model.user.UserProfile;
import model.user.Settings;
import dao.Implementations.UserProfileDAO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TC01_SuccessfulUserProfileCreationTest {

	@Test
    public void testUserProfileCreationAndPersistence() {
        // Setup profile using valid constructor and types
        Settings settings = new Settings("metric");
        UserProfile profile = new UserProfile("Alice", "Female", LocalDate.of(1990, 1, 1), 165, 60);
        profile.setSettings(settings);
        profile.setUserID(1);  // ID required for retrieval

        // Simulate saving to database
        UserProfileDAO dao = new UserProfileDAO();
        dao.saveUserProfile(profile);  // returns void

        // Re-load profile to confirm persistence
        UserProfile loaded = dao.getUserProfile(1);  // now uses int

        assertNotNull(loaded);
        assertEquals("Alice", loaded.getName());
        assertEquals("Female", loaded.getSex());
        assertEquals(LocalDate.of(1990, 1, 1), loaded.getDob());
        assertEquals(165, loaded.getHeight());
        assertEquals(60, loaded.getWeight());
        assertEquals("metric", loaded.getSettings().getUnits());
    }
}