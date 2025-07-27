package strategy;

import model.FoodItem;
import java.util.List;

public class ProteinSwapStrategy implements RecommendationStrategy {
    
    @Override
    public FoodItem recommendReplacement(FoodItem original, List<FoodItem> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        
        // Find the candidate with the highest protein content
        FoodItem bestCandidate = candidates.get(0);
        double bestProtein = getProteinContent(bestCandidate);
        
        for (FoodItem candidate : candidates) {
            double protein = getProteinContent(candidate);
            if (protein > bestProtein) {
                bestProtein = protein;
                bestCandidate = candidate;
            }
        }
        
        // Only recommend if the replacement has significantly more protein
        double originalProtein = getProteinContent(original);
        if (bestProtein > originalProtein * 1.2) { // 20% improvement threshold
            return bestCandidate;
        }
        
        return null;
    }
    
    private double getProteinContent(FoodItem food) {
        return food.getNutrients().getOrDefault("Protein", 0.0);
    }
} 