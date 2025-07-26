package strategy;

import model.FoodItem;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy to recommend a lower-fat food item from the same group.
 */
public class FatSwapStrategy implements RecommendationStrategy {

    @Override
    public FoodItem recommendReplacement(FoodItem original, List<FoodItem> candidates) {
        double originalFat = original.getNutrients().getOrDefault("Fat", 0.0);
        
        return candidates.stream()
                .filter(item -> {
                    double itemFat = item.getNutrients().getOrDefault("Fat", 0.0);
                    return itemFat < originalFat;
                })
                .min(Comparator.comparingDouble(item -> item.getNutrients().getOrDefault("Fat", 0.0)))
                .orElse(null);
    }
} 