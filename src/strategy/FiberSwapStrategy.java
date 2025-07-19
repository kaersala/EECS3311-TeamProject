package strategy;

import model.FoodItem;

import java.util.Comparator;
import java.util.List;

/**
 * Strategy to recommend a higher-fiber food item from the same group.
 */
public class FiberSwapStrategy implements RecommendationStrategy {

    @Override
    public FoodItem recommendReplacement(FoodItem original, List<FoodItem> candidates) {
        double originalFiber = original.getNutrients().getOrDefault("Fiber", 0.0);

        return candidates.stream()
                .filter(item -> item.getNutrients().containsKey("Fiber") &&
                        item.getNutrients().get("Fiber") > originalFiber)
                .max(Comparator.comparingDouble(item -> item.getNutrients().getOrDefault("Fiber", 0.0)))
                .orElse(null);
    }
}
