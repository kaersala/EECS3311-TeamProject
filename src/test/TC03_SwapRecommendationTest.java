package test;

import dao.adapter.MySQLAdapter;
import dao.Implementations.UserProfileDAO;
import model.meal.*;
import model.user.UserProfile;
import model.user.Settings;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TC03_SwapRecommendationTest {

    @Test
    public void testSwapRecommendationExecution() {
        MySQLAdapter adapter = new MySQLAdapter();
        adapter.connect();

        int userId = 1;
        LocalDate today = LocalDate.now();

     // Ensure user exists and has settings
        UserProfileDAO userDao = new UserProfileDAO();
        UserProfile profile = userDao.getUserProfile(userId);

        // Clean up any existing meals to reset test state
        List<Meal> existingMeals = adapter.loadMeals(userId);
        for (Meal m : existingMeals) {
            adapter.deleteMeal(m.getMealID()); // make sure this method exists
        }
        if (profile == null) {
            profile = new UserProfile("Alice", "Female", LocalDate.of(1990, 1, 1), 165, 60);
            profile.setUserID(userId);
            profile.setSettings(new Settings("metric"));
            userDao.saveUserProfile(profile);
        }

        // Step 1–4: Log a low-protein meal for past 3 days
        for (int i = 0; i < 3; i++) {
            LocalDate date = today.minusDays(i);
            List<IngredientEntry> ingredients = Arrays.asList(
                    new IngredientEntry(1001, 100),  // Apple – very low protein
                    new IngredientEntry(2001, 150)   // White rice – low protein
            );
            Meal meal = new Meal(0, userId, date, MealType.LUNCH, ingredients);
            adapter.saveMeal(meal);
        }

        // Step 6: Load meals from DB
        List<Meal> meals = adapter.loadMeals(userId);
        assertFalse(meals.isEmpty(), "User should have logged meals in past 3 days");

        List<Meal> recentMeals = meals.stream()
                .filter(m -> !m.getDate().isBefore(today.minusDays(3)))
                .collect(Collectors.toList());

        assertFalse(recentMeals.isEmpty(), "Recent meals should be available");

        // Step 7: Calculate protein + calories BEFORE swap
        double proteinBefore = 0;
        double caloriesBefore = 0;

        for (Meal meal : recentMeals) {
            for (IngredientEntry ing : meal.getIngredients()) {
                proteinBefore += adapter.getNutrientsForFood(ing.getFoodID()).entrySet().stream()
                        .filter(e -> e.getKey().equalsIgnoreCase("Protein"))
                        .mapToDouble(e -> e.getValue() * (ing.getQuantity() / 100.0))
                        .sum();

                caloriesBefore += adapter.getCaloriesForFood(ing.getFoodID()) * (ing.getQuantity() / 100.0);
            }
        }

        // Step 7d: Swap to high-protein foods
        List<IngredientEntry> improvedIngredients = Arrays.asList(
                new IngredientEntry(4234, 100), // Gelatin powder (85.6g protein)
                new IngredientEntry(86, 100)    // Egg white powder (82.4g protein)
        );
        Meal swappedMeal = new Meal(0, userId, today, MealType.LUNCH, improvedIngredients);
        adapter.saveMeal(swappedMeal);

        // Step 8: Calculate protein + calories AFTER swap
        double proteinAfter = 0;
        double caloriesAfter = 0;

        for (IngredientEntry ing : improvedIngredients) {
            proteinAfter += adapter.getNutrientsForFood(ing.getFoodID()).entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase("Protein"))
                    .mapToDouble(e -> e.getValue() * (ing.getQuantity() / 100.0))
                    .sum();

            caloriesAfter += adapter.getCaloriesForFood(ing.getFoodID()) * (ing.getQuantity() / 100.0);
        }

        double proteinDelta = proteinAfter - proteinBefore;
        double calorieDelta = caloriesAfter - caloriesBefore;

        // Step 9: Debug Print
        System.out.printf("Protein Before: %.2f g%n", proteinBefore);
        System.out.printf("Protein After:  %.2f g%n", proteinAfter);
        System.out.printf("Calories Before: %.2f kcal%n", caloriesBefore);
        System.out.printf("Calories After:  %.2f kcal%n", caloriesAfter);
        System.out.printf("Swap Results: ΔProtein = %.2f g, ΔCalories = %.2f kcal%n", proteinDelta, calorieDelta);

        // Assertions
        assertTrue(proteinDelta >= 10.0, "Protein should increase by at least 10g");

        System.out.println("Swap recommendation test passed with nutrient improvements.");
    }
}
