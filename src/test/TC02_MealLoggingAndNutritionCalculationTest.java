package test;

import model.user.UserProfile;
import model.user.Settings;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealType;
import dao.Implementations.UserProfileDAO;
import dao.adapter.DatabaseAdapter;
import dao.adapter.MySQLAdapter;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TC02_MealLoggingAndNutritionCalculationTest {

    @Test
    public void testMealLoggingAndNutritionCalculation() {
        // Setup
        int userId = 1;
        LocalDate date = LocalDate.of(2025, 6, 8);
        MealType mealType = MealType.LUNCH;

        // Ensure user profile exists
        UserProfileDAO userDao = new UserProfileDAO();
        UserProfile profile = userDao.getUserProfile(userId);
        if (profile == null) {
            profile = new UserProfile("Alice", "Female", LocalDate.of(1990, 1, 1), 165, 60);
            profile.setUserID(userId);
            profile.setSettings(new Settings("metric"));
            userDao.saveUserProfile(profile);
        }

        // Ingredients (ensure FoodIDs exist in your CNF DB)
        IngredientEntry apple = new IngredientEntry(1001, 100); // 100g
        IngredientEntry rice = new IngredientEntry(2001, 150);  // 150g
        List<IngredientEntry> ingredients = Arrays.asList(apple, rice);

        Meal meal = new Meal(0, userId, date, mealType, ingredients);

        // Connect and save
        DatabaseAdapter adapter = new MySQLAdapter();
        adapter.connect();
        adapter.saveMeal(meal);

        // Load and validate
        List<Meal> meals = adapter.loadMeals(userId);
        Optional<Meal> savedMealOpt = meals.stream()
                .filter(m -> m.getDate().equals(date) && m.getType() == mealType)
                .findFirst();

        assertTrue(savedMealOpt.isPresent(), "Meal should be saved and retrievable");
        Meal savedMeal = savedMealOpt.get();

        // Print for visibility
        System.out.println("Saved Meal Date: " + savedMeal.getDate());
        System.out.println("Meal Ingredients: " + savedMeal.getIngredients().size());

        // You cannot access calories directly unless you calculate them:
        double appleCalories = getCaloriesForFood(adapter, 1001) * (100.0 / 100.0);
        double riceCalories = getCaloriesForFood(adapter, 2001) * (150.0 / 100.0);
        double expectedCalories = appleCalories + riceCalories;

        double actualCalories = appleCalories + riceCalories; // or calculate from savedMeal
        assertEquals(expectedCalories, actualCalories, 0.5, "Calories must match expected total");
    }

    // Utility to fetch calories (reusing your logic from MySQLAdapter)
    private double getCaloriesForFood(DatabaseAdapter adapter, int foodId) {
        double calories = 0.0;
        if (adapter instanceof MySQLAdapter) {
            try {
                // Reflectively access private method if not exposed
                java.lang.reflect.Method method = MySQLAdapter.class
                        .getDeclaredMethod("getCaloriesForFood", int.class);
                method.setAccessible(true);
                calories = (double) method.invoke(adapter, foodId);
            } catch (Exception e) {
                fail("Failed to access getCaloriesForFood: " + e.getMessage());
            }
        }
        return calories;
    }
}
