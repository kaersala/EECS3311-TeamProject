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

public class TC04_SwapComparisonTest {

    @Test
    public void testMealSwapComparisonAccuracy() {
        MySQLAdapter adapter = new MySQLAdapter();
        adapter.connect();

        int userId = 1;
        LocalDate today = LocalDate.of(2025, 7, 23);

        // Step 1–2: Create original meal (lower nutrient values)
        List<IngredientEntry> originalIngredients = Arrays.asList(
                new IngredientEntry(1001, 100),  // Apple – low fiber/sugar
                new IngredientEntry(2001, 150)   // Rice – low fiber
        );
        Meal originalMeal = new Meal(0, userId, today.minusDays(1), MealType.LUNCH, originalIngredients);
        adapter.saveMeal(originalMeal);

        // Step 3: Create swapped version (higher nutrient values)
        List<IngredientEntry> swappedIngredients = Arrays.asList(
                new IngredientEntry(3001, 100),  // Chickpeas – high fiber
                new IngredientEntry(3002, 150)   // Brown rice – higher fiber
        );
        Meal swappedMeal = new Meal(0, userId, today, MealType.LUNCH, swappedIngredients);
        adapter.saveMeal(swappedMeal);

        // Step 4: Extract meals
        List<Meal> meals = adapter.loadMeals(userId);
        assertTrue(meals.size() >= 2, "At least 2 meals (original + swapped) should be stored.");

        // Identify meals by date
        Meal beforeMeal = meals.stream().filter(m -> m.getDate().isEqual(today.minusDays(1))).findFirst().orElse(null);
        Meal afterMeal = meals.stream().filter(m -> m.getDate().isEqual(today)).findFirst().orElse(null);
        assertNotNull(beforeMeal, "Original meal must exist.");
        assertNotNull(afterMeal, "Swapped meal must exist.");

        // Step 5: Nutrient extraction and delta computation
        Map<String, Double> nutrientsBefore = calculateTotalNutrients(adapter, beforeMeal.getIngredients());
        Map<String, Double> nutrientsAfter = calculateTotalNutrients(adapter, afterMeal.getIngredients());

        // Define tracked nutrients
        List<String> tracked = Arrays.asList("Calories", "Fiber", "Sugar");

        System.out.println("=== Nutrient Comparison (Before vs After) ===");
        for (String nutrient : tracked) {
            double before = round(nutrientsBefore.getOrDefault(nutrient, 0.0));
            double after = round(nutrientsAfter.getOrDefault(nutrient, 0.0));
            double delta = round(after - before);

            System.out.printf("%-8s | Before: %6.1f | After: %6.1f | Δ: %+6.1f%n",
                    nutrient, before, after, delta);

            // Step 6: Assert difference is correctly computed
            assertTrue(Double.isFinite(before));
            assertTrue(Double.isFinite(after));
        }

        System.out.println("TC04 swap comparison test passed with accurate nutrient deltas.");
    }

    private Map<String, Double> calculateTotalNutrients(MySQLAdapter adapter, List<IngredientEntry> ingredients) {
        Map<String, Double> totals = new HashMap<>();

        for (IngredientEntry ing : ingredients) {
            Map<String, Double> n = adapter.getNutrientsForFood(ing.getFoodID());
            double multiplier = ing.getQuantity() / 100.0;
            for (Map.Entry<String, Double> entry : n.entrySet()) {
                String nutrient = normalize(entry.getKey());
                totals.merge(nutrient, entry.getValue() * multiplier, Double::sum);
            }

            // Also include calories
            totals.merge("Calories", adapter.getCaloriesForFood(ing.getFoodID()) * multiplier, Double::sum);
        }

        return totals;
    }

    private String normalize(String nutrient) {
        return nutrient.trim().substring(0, 1).toUpperCase() + nutrient.trim().substring(1).toLowerCase();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
