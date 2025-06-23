package backend;

import model.*;
import model.meal.IngredientEntry;
import strategy.*;

import java.util.*;

public class SwapEngine {

    public List<SwapSuggestion> generateSwaps(List<Goal> goals, List<IngredientEntry> currentMeal, Map<Integer, FoodItem> foodDatabase) {
        List<SwapSuggestion> suggestions = new ArrayList<>();

        for (Goal goal : goals) {
            double intensityFactor = getIntensityFactor(goal.getIntensity());
            RecommendationStrategy strategy = selectStrategy(goal);

            if (strategy == null) continue;

            for (IngredientEntry entry : currentMeal) {
                FoodItem original = foodDatabase.get(entry.getFoodID());

                // Filter candidate replacements (same group, not same item)
                List<FoodItem> candidates = foodDatabase.values().stream()
                        .filter(item -> item.getFoodGroup().equals(original.getFoodGroup()))
                        .filter(item -> item.getFoodID() != original.getFoodID())
                        .toList();

                FoodItem replacement = strategy.recommendReplacement(original, candidates);

                if (replacement != null) {
                    SwapSuggestion suggestion = new SwapSuggestion(entry, new IngredientEntry(replacement.getFoodID(), entry.getQuantity()),
                            "Improves " + goal.getNutrient() + " (" + goal.getDirection() + ")");
                    suggestions.add(suggestion);
                }
            }
        }

        return suggestions;
    }

    private RecommendationStrategy selectStrategy(Goal goal) {
        return switch (goal.getNutrient().toLowerCase()) {
            case "calories" -> new CalorieSwapStrategy();
            case "fiber" -> new FiberSwapStrategy();
            // add more cases like sodium, protein etc.
            default -> null;
        };
    }

    private double getIntensityFactor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "moderate" -> 1.25;
            case "high" -> 1.5;
            default -> 1.0;
        };
    }
}
