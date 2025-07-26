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

public class TC06_NutrientVisualizationTest {

    @Test
    public void testNutrientVisualizationSummary() {
        MySQLAdapter adapter = new MySQLAdapter();
        adapter.connect();

        int userId = 106;
        LocalDate today = LocalDate.now();

        // Ensure test user exists
        UserProfileDAO userDao = new UserProfileDAO();
        UserProfile profile = userDao.getUserProfile(userId);
        if (profile == null) {
            profile = new UserProfile("NutrientUser", "Female", LocalDate.of(1990, 1, 1), 165, 60);
            profile.setUserID(userId);
            profile.setSettings(new Settings("metric"));
            userDao.saveUserProfile(profile);
        }

        // Log 7 days of meals with varied nutrient content
        Random rand = new Random();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(i);
            List<IngredientEntry> ingredients = Arrays.asList(
                    new IngredientEntry(86, 100 + rand.nextInt(50)),   // Egg powder – protein
                    new IngredientEntry(1001, 50 + rand.nextInt(50)),  // Apple – sugar
                    new IngredientEntry(2001, 100 + rand.nextInt(30))  // Rice – carbs
            );
            Meal meal = new Meal(0, userId, date, MealType.LUNCH, ingredients);
            adapter.saveMeal(meal);
        }

        // Load meals for the time period
        List<Meal> meals = adapter.loadMeals(userId).stream()
                .filter(m -> !m.getDate().isBefore(today.minusDays(6)))
                .collect(Collectors.toList());

        assertEquals(7, meals.size(), "Expected 7 meals for visualization");

        // Aggregate nutrients per day
        Map<LocalDate, Map<String, Double>> dailyTotals = new TreeMap<>();
        for (Meal meal : meals) {
            Map<String, Double> nutrients = dailyTotals.computeIfAbsent(meal.getDate(), k -> new HashMap<>());
            for (IngredientEntry ing : meal.getIngredients()) {
                Map<String, Double> foodNutrients = adapter.getNutrientsForFood(ing.getFoodID());
                for (Map.Entry<String, Double> e : foodNutrients.entrySet()) {
                    String nutrient = e.getKey();
                    double value = e.getValue() * (ing.getQuantity() / 100.0);
                    nutrients.put(nutrient, nutrients.getOrDefault(nutrient, 0.0) + value);
                }
            }
        }

        // Aggregate over all days
        Map<String, Double> totalPerNutrient = new HashMap<>();
        for (Map<String, Double> daily : dailyTotals.values()) {
            for (Map.Entry<String, Double> e : daily.entrySet()) {
                totalPerNutrient.put(e.getKey(), totalPerNutrient.getOrDefault(e.getKey(), 0.0) + e.getValue());
            }
        }

        // Compute average and percentage breakdown
        Map<String, Double> averagePerDay = new HashMap<>();
        double grandTotal = 0;
        for (Map.Entry<String, Double> entry : totalPerNutrient.entrySet()) {
            double avg = entry.getValue() / 7.0;
            averagePerDay.put(entry.getKey(), avg);
            grandTotal += avg;
        }

        // Sort by contribution
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(averagePerDay.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        System.out.println("=== Nutrient Intake Summary (Avg per Day) ===");
        int count = 0;
        double topTotal = 0;
        for (Map.Entry<String, Double> entry : sorted) {
            double percent = (entry.getValue() / grandTotal) * 100.0;
            System.out.printf("%s: %.1f g (%.1f%%)\n", entry.getKey(), entry.getValue(), percent);
            count++;
            if (count == 10) break;
            topTotal += percent;
        }
        System.out.printf("Other nutrients: %.1f%%\n", 100 - topTotal);

        // Mock comparison with RDI (Recommended Daily Intake)
        Map<String, Double> mockRDI = Map.of(
                "Protein", 50.0,
                "Sugar", 25.0,
                "Fiber", 28.0
        );

        System.out.println("\n=== RDI Comparison ===");
        for (Map.Entry<String, Double> entry : mockRDI.entrySet()) {
            double actual = averagePerDay.getOrDefault(entry.getKey(), 0.0);
            double rdi = entry.getValue();
            double ratio = (actual / rdi) * 100.0;
            System.out.printf("%s: %.1f g (%.1f%% of RDI) %s\n",
                    entry.getKey(), actual, ratio,
                    ratio < 80 ? " Low" : ratio > 120 ? "High" : "OK");
        }

        // Assertions (example)
        assertTrue(averagePerDay.size() > 0, "Average nutrient map should not be empty");
        assertTrue(grandTotal > 0, "Total nutrient intake should be > 0");
    }
}
