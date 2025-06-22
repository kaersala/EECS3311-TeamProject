package backend;

import model.Goal;
import model.FoodItem;
import model.meal.Meal;

import java.util.List;
import java.util.Map;

public class GoalChecker {

    public boolean isGoalAchieved(Goal goal, Meal meal, Map<Integer, FoodItem> foodDatabase) {
        Map<String, Double> totalNutrients = calculateTotalNutrients(meal, foodDatabase);
        String nutrient = goal.getNutrient();
        String direction = goal.getDirection().toLowerCase();
        double intensityFactor = getIntensityFactor(goal.getIntensity());

        double actualAmount = totalNutrients.getOrDefault(nutrient, 0.0);
        double targetAmount = goal.getAmount() * intensityFactor;

        if (direction.equals("increase")) {
            return actualAmount >= targetAmount;
        } else if (direction.equals("decrease")) {
            return actualAmount <= targetAmount;
        }

        return false; // Unknown direction
    }

    private double getIntensityFactor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "moderate" -> 1.25;
            case "high" -> 1.5;
            default -> 1.0;
        };
    }

    private Map<String, Double> calculateTotalNutrients(Meal meal, Map<Integer, FoodItem> foodDatabase) {
        Map<String, Double> totals = new java.util.HashMap<>();

        for (var entry : meal.getIngredients()) {
            FoodItem item = foodDatabase.get(entry.getFoodID());
            double quantity = entry.getQuantity();

            if (item == null || item.getNutrients() == null) continue;

            for (var nutrient : item.getNutrients().entrySet()) {
                String name = nutrient.getKey();
                double amount = nutrient.getValue();
                totals.merge(name, amount * (quantity / 100.0), Double::sum); // assuming per 100g
            }
        }

        return totals;
    }
}
