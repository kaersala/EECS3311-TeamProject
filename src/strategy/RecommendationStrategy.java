package strategy;

import model.FoodItem;
import java.util.List;

/**
 * Interface for implementing different food replacement strategies
 * based on nutritional goals.
 */
public interface RecommendationStrategy {

    /**
     * Recommends a replacement FoodItem from a list of candidates
     * based on the nutritional objective (e.g., reduce calories, increase fiber).
     *
     * @param original  the original FoodItem to be replaced
     * @param candidates list of possible FoodItem replacements
     * @return the recommended replacement FoodItem, or null if no suitable match is found
     */
    FoodItem recommendReplacement(FoodItem original, List<FoodItem> candidates);
}
