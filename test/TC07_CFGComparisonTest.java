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

public class TC07_CFGComparisonTest {

    @Test
    public void testCFGAlignmentWithGuidelines() {
        MySQLAdapter adapter = new MySQLAdapter();
        adapter.connect();

        int userId = 107;
        LocalDate today = LocalDate.now();

        // Ensure test user exists
        UserProfileDAO userDao = new UserProfileDAO();
        UserProfile profile = userDao.getUserProfile(userId);
        if (profile == null) {
            profile = new UserProfile("CFGUser", "Other", LocalDate.of(1990, 1, 1), 170, 65);
            profile.setUserID(userId);
            profile.setSettings(new Settings("metric"));
            userDao.saveUserProfile(profile);
        }

        // Cleanup previous meals for user
        List<Meal> allMeals = adapter.loadMeals(userId);
        for (Meal m : allMeals) {
            adapter.deleteMeal(m.getMealID());
        }

        // Step 1–2: Log 5 meals over the last 5 days with diverse food groups
        Map<Integer, String> foodGroups = Map.of(
                3001, "Vegetables",   // Carrot
                4001, "Grains",       // Rice
                5001, "Protein",      // Chicken
                6001, "Fruits",       // Banana
                7001, "Dairy"         // Yogurt
        );

        for (int i = 0; i < 5; i++) {
            LocalDate date = today.minusDays(i);
            List<IngredientEntry> ingredients = new ArrayList<>();
            for (int foodId : foodGroups.keySet()) {
                ingredients.add(new IngredientEntry(foodId, 100)); // 100g each
            }
            Meal meal = new Meal(0, userId, date, MealType.DINNER, ingredients);
            adapter.saveMeal(meal);
        }

        // Step 3: Load recent meals for the last 5 days
        List<Meal> meals = adapter.loadMeals(userId).stream()
                .filter(m -> !m.getDate().isBefore(today.minusDays(4)))
                .collect(Collectors.toList());

        assertEquals(5, meals.size(), "Expected 5 meals for CFG analysis");

        // Step 4–5: Classify and calculate food group totals
        Map<String, Double> groupTotals = new HashMap<>();
        for (Meal meal : meals) {
            for (IngredientEntry ing : meal.getIngredients()) {
                String group = foodGroups.getOrDefault(ing.getFoodID(), "Other");
                groupTotals.put(group, groupTotals.getOrDefault(group, 0.0) + ing.getQuantity());
            }
        }

        double totalGrams = groupTotals.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<String, Integer> groupPercentages = new HashMap<>();
        for (Map.Entry<String, Double> entry : groupTotals.entrySet()) {
            int percent = (int) Math.round((entry.getValue() / totalGrams) * 100);
            groupPercentages.put(entry.getKey(), percent);
        }

        // Step 6: Define CFG plate targets
        Map<String, Integer> cfgPlate = Map.of(
                "Vegetables", 50,
                "Grains", 25,
                "Protein", 25
        );

        // Step 7–9: Visual comparison simulation
        System.out.println("\n--- CFG Comparison ---");
        for (String group : cfgPlate.keySet()) {
            int userPercent = groupPercentages.getOrDefault(group, 0);
            int target = cfgPlate.get(group);
            System.out.printf("%s: %d%% (target: %d%%)%n", group, userPercent, target);
        }

        // Step 10: Assertions
        assertTrue(groupPercentages.containsKey("Vegetables"), "Vegetables group must be included");
        assertTrue(groupPercentages.containsKey("Grains"), "Grains group must be included");
        assertTrue(groupPercentages.containsKey("Protein"), "Protein group must be included");

        System.out.println("CFG alignment comparison completed.");
    }
}
