package test;

import dao.adapter.MySQLAdapter;
import dao.Implementations.*;
import model.user.UserProfile;
import model.meal.Meal;
import model.meal.MealType;
import model.meal.IngredientEntry;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TC08_SwapImpactVisualizationTest {

    @Test
    public void testSwapImpactNutrientVisualization() {
        MySQLAdapter adapter = new MySQLAdapter();
        adapter.connect();

        int userId = 2025;
        LocalDate today = LocalDate.now();

        // === Step 1: Ensure user exists ===
        UserProfileDAO userDao = new UserProfileDAO();
        UserProfile profile = userDao.getUserProfile(userId);
        if (profile == null) {
            profile = new UserProfile("SwapUser", "F", today.minusYears(25), 165.0, 60.0);
            profile.setUserID(userId);
            userDao.saveUserProfile(profile);
        }

        userId = 107;
        int highProteinFoodId = 86;    // Egg white, dried (82.4g protein)
        int lowProteinFoodId = 4234;   // Gelatin (85.6g protein – actually quite high)


        Meal originalMeal = new Meal(0, userId, today.minusDays(1), MealType.DINNER, new ArrayList<>());
        originalMeal.getIngredients().add(new IngredientEntry(highProteinFoodId, 1.0));
        adapter.saveMeal(originalMeal);

        Meal swappedMeal = new Meal(0, userId, today.minusDays(1), MealType.DINNER, new ArrayList<>());
        swappedMeal.getIngredients().add(new IngredientEntry(lowProteinFoodId, 1.0));
        adapter.saveMeal(swappedMeal);

        // === Step 4: Fetch meals and compute protein totals ===
        List<Meal> meals = adapter.loadMeals(userId);
        double totalProteinBefore = 0.0;
        double totalProteinAfter = 0.0;

        for (Meal meal : meals) {
            if (!meal.getDate().equals(today.minusDays(1))) continue;

            for (IngredientEntry entry : meal.getIngredients()) {
                Map<String, Double> nutrients = adapter.getNutrientsForFood(entry.getFoodID());

                double proteinValue = 0.0;
                for (String key : nutrients.keySet()) {
                    if (key.toLowerCase().contains("protein")) {
                        proteinValue = nutrients.get(key);
                        break;
                    }
                }

                double proteinTotal = proteinValue * entry.getQuantity();

                if (entry.getFoodID() == highProteinFoodId) {
                    totalProteinBefore += proteinTotal;
                } else if (entry.getFoodID() == lowProteinFoodId) {
                    totalProteinAfter += proteinTotal;
                }
            }
        }

        double delta = totalProteinAfter - totalProteinBefore;

        // === Debug output ===
        System.out.println("=== Swap Impact Summary ===");
        System.out.printf("Protein: Before = %.1f, After = %.1f, Δ = %.1f%n", totalProteinBefore, totalProteinAfter, delta);

        // === Step 5: Assertion ===
        assertNotEquals(totalProteinBefore, totalProteinAfter, 0.1, "Protein should show measurable change after swap.");
    }
}
