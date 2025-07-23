package test;

import dao.adapter.MySQLAdapter;
import dao.Implementations.UserProfileDAO;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealType;
import model.user.Settings;
import model.user.UserProfile;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TC10_DataPersistenceTest {

    @Test
    public void testMealDataPersistence() {
        MySQLAdapter adapter = new MySQLAdapter();
        adapter.connect();

        // === Ensure user profile exists ===
        int testUserId = 888;
        UserProfileDAO userDao = new UserProfileDAO();
        UserProfile profile = userDao.getUserProfile(testUserId);
        if (profile == null) {
            profile = new UserProfile("TestUser", "M", LocalDate.of(1990, 1, 1) , 170, 70.0);
            profile.setUserID(testUserId);
            profile.setSettings(new Settings("metric"));
            userDao.saveUserProfile(profile);
        }

        // === Step 1: Save test meal ===
        LocalDate date = LocalDate.now();
        MealType type = MealType.LUNCH;
        List<IngredientEntry> ingredients = new ArrayList<>();
        ingredients.add(new IngredientEntry(86, 1.5));     // Egg white
        ingredients.add(new IngredientEntry(4234, 0.5));   // Gelatin

        Meal mealToSave = new Meal(0, testUserId, date, type, ingredients);
        adapter.saveMeal(mealToSave);

        // === Step 2: Simulate application reset ===
        mealToSave = null;

        // === Step 3: Load and verify ===
        List<Meal> meals = adapter.loadMeals(testUserId);
        assertFalse(meals.isEmpty(), "No meals loaded for test user.");

        Meal loadedMeal = meals.stream()
            .filter(m -> m.getDate().equals(date) && m.getType() == type)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Saved meal not found"));

        // === Step 4: Compare fields ===
        assertEquals(testUserId, loadedMeal.getUserID());
        assertEquals(date, loadedMeal.getDate());
        assertEquals(type, loadedMeal.getType());

        List<IngredientEntry> loadedIngredients = loadedMeal.getIngredients();
        assertEquals(2, loadedIngredients.size(), "Ingredient count mismatch.");

        for (IngredientEntry entry : ingredients) {
            boolean foundMatch = loadedIngredients.stream().anyMatch(
                e -> e.getFoodID() == entry.getFoodID() &&
                     Double.compare(e.getQuantity(), entry.getQuantity()) == 0
            );
            assertTrue(foundMatch, "Ingredient mismatch for FoodID: " + entry.getFoodID());
        }

        System.out.println("✔ Meal saved and loaded correctly.");
    }
}
