package strategy;

import model.FoodItem;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy to recommend a lower-calorie food item from the same group.
 */
public class CalorieSwapStrategy implements RecommendationStrategy {

    @Override
    public FoodItem recommendReplacement(FoodItem original, List<FoodItem> candidates) {
        return candidates.stream()
                .filter(item -> item.getCalories() < original.getCalories())
                .min(Comparator.comparingDouble(FoodItem::getCalories))
                .orElse(null);
    }
}
