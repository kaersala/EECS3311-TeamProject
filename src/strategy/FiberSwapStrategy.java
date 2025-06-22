package strategy;

import model.FoodItem;
import java.util.Comparator;
import java.util.List;

public class FiberSwapStrategy implements RecommendationStrategy {

    @Override
    public FoodItem recommendReplacement(FoodItem original, List<FoodItem> candidates) {
        return candidates.stream()
                .filter(item -> item.getNutrients().containsKey("Fiber") &&
                        item.getNutrients().get("Fiber") > original.getNutrients().getOrDefault("Fiber", 0.0))
                .max(Comparator.comparingDouble(item -> item.getNutrients().getOrDefault("Fiber", 0.0)))
                .orElse(null);
    }
}
