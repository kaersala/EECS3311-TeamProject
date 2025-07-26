package strategy;

import model.FoodItem;
import java.util.List;

public class CarbsSwapStrategy implements RecommendationStrategy {
    
    @Override
    public FoodItem recommendReplacement(FoodItem original, List<FoodItem> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        
        // Find the candidate with the lowest carbs content (for decrease goal)
        FoodItem bestCandidate = candidates.get(0);
        double bestCarbs = getCarbsContent(bestCandidate);
        
        for (FoodItem candidate : candidates) {
            double carbs = getCarbsContent(candidate);
            if (carbs < bestCarbs) {
                bestCarbs = carbs;
                bestCandidate = candidate;
            }
        }
        
        // Only recommend if the replacement has significantly fewer carbs
        double originalCarbs = getCarbsContent(original);
        if (bestCarbs < originalCarbs * 0.8) { // 20% reduction threshold
            return bestCandidate;
        }
        
        return null;
    }
    
    private double getCarbsContent(FoodItem food) {
        return food.getNutrients().getOrDefault("Carbs", 0.0);
    }
} 