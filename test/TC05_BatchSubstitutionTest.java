package test;

import dao.adapter.MySQLAdapter;
import model.meal.*;
import model.user.Settings;
import model.user.UserProfile;
import dao.Implementations.UserProfileDAO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TC05_BatchSubstitutionTest {

    @Test
    public void testBatchSubstitutionAcrossMeals() {
        MySQLAdapter adapter = new MySQLAdapter();
        adapter.connect();

        int userId = 1;
        adapter.deleteAllMealsForUser(userId);        
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(7);

        // Ensure user exists
        UserProfileDAO userDao = new UserProfileDAO();
        UserProfile profile = userDao.getUserProfile(userId);
        if (profile == null) {
            profile = new UserProfile("BatchUser", "Female", LocalDate.of(1990, 1, 1), 160, 60);
            profile.setUserID(userId);
            profile.setSettings(new Settings("metric"));
            userDao.saveUserProfile(profile);
        }

        // Define substitution: White Bread → Whole Wheat Bread
        int originalFoodId = 5001; // White Bread
        int substituteFoodId = 5002; // Whole Wheat Bread

        // Step 1–3: Log meals over 7 days, some with target food
        for (int i = 1; i <= 7; i++) {
            LocalDate mealDate = today.minusDays(i);
            List<IngredientEntry> ingredients = new ArrayList<>();
            if (i % 2 == 0) {
                // Include white bread
                ingredients.add(new IngredientEntry(originalFoodId, 50));
            }
            ingredients.add(new IngredientEntry(1001, 100)); // Apple or other static item
            Meal meal = new Meal(0, userId, mealDate, MealType.BREAKFAST, ingredients);
            adapter.saveMeal(meal);
        }

        // Step 4: Fetch meals in date range
        List<Meal> meals = adapter.loadMeals(userId).stream()
                .filter(m -> !m.getDate().isBefore(startDate) && !m.getDate().isAfter(today))
                .collect(Collectors.toList());

        assertFalse(meals.isEmpty(), "Meals should exist in the last 7 days");

        double totalCaloriesBefore = 0, totalFiberBefore = 0;
        double totalCaloriesAfter = 0, totalFiberAfter = 0;
        int mealsUpdated = 0;

        // Step 5: Apply batch substitution logic
        for (Meal meal : meals) {
            boolean modified = false;
            List<IngredientEntry> updatedIngredients = new ArrayList<>();

            for (IngredientEntry ing : meal.getIngredients()) {
                if (ing.getFoodID() == originalFoodId) {
                    updatedIngredients.add(new IngredientEntry(substituteFoodId, ing.getQuantity()));
                    modified = true;
                } else {
                    updatedIngredients.add(ing);
                }

                // Nutrient before
                Map<String, Double> nutrients = adapter.getNutrientsForFood(ing.getFoodID());
                totalCaloriesBefore += adapter.getCaloriesForFood(ing.getFoodID()) * (ing.getQuantity() / 100.0);
                totalFiberBefore += nutrients.getOrDefault("Fiber", 0.0) * (ing.getQuantity() / 100.0);
            }

            if (modified) {
                Meal updatedMeal = new Meal(meal.getMealID(), userId, meal.getDate(), meal.getType(), updatedIngredients);
                adapter.saveMeal(updatedMeal); // Overwrite existing
                mealsUpdated++;

                for (IngredientEntry ing : updatedIngredients) {
                    Map<String, Double> nutrients = adapter.getNutrientsForFood(ing.getFoodID());
                    totalCaloriesAfter += adapter.getCaloriesForFood(ing.getFoodID()) * (ing.getQuantity() / 100.0);
                    totalFiberAfter += nutrients.getOrDefault("Fiber", 0.0) * (ing.getQuantity() / 100.0);
                }
            } else {
                // Unchanged
                for (IngredientEntry ing : meal.getIngredients()) {
                    Map<String, Double> nutrients = adapter.getNutrientsForFood(ing.getFoodID());
                    totalCaloriesAfter += adapter.getCaloriesForFood(ing.getFoodID()) * (ing.getQuantity() / 100.0);
                    totalFiberAfter += nutrients.getOrDefault("Fiber", 0.0) * (ing.getQuantity() / 100.0);
                }
            }
        }

        // Step 7: Compute and print deltas
        double deltaCalories = totalCaloriesAfter - totalCaloriesBefore;
        double deltaFiber = totalFiberAfter - totalFiberBefore;
        double avgCalorieChange = deltaCalories / meals.size();
        double avgFiberChange = deltaFiber / meals.size();

        System.out.printf("Updated Meals: %d%n", mealsUpdated);
        System.out.printf("Cumulative ΔCalories = %.1f kcal, ΔFiber = %.1f g%n", deltaCalories, deltaFiber);
        System.out.printf("Average ΔCalories = %.2f kcal/meal, ΔFiber = %.2f g/meal%n", avgCalorieChange, avgFiberChange);

        // Step 8: Assertions
        assertTrue(mealsUpdated > 0, "At least one meal should be updated by batch substitution");
        assertEquals(7, meals.size(), "Should process exactly 7 meals from past week");
    }
}
