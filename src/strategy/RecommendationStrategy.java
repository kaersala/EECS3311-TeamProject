package strategy;

import model.FoodItem;
import java.util.List;

public interface RecommendationStrategy {
    FoodItem recommendReplacement(FoodItem original, List<FoodItem> candidates);
}
