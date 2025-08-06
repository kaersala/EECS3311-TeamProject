package strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StrategyFactory {
    private static final Map<String, Supplier<RecommendationStrategy>> strategyCreators = new HashMap<>();
    
    static {
        strategyCreators.put("calories", CalorieSwapStrategy::new);
        strategyCreators.put("protein", ProteinSwapStrategy::new);
        strategyCreators.put("carbs", CarbsSwapStrategy::new);
        strategyCreators.put("fat", FatSwapStrategy::new);
        strategyCreators.put("fiber", FiberSwapStrategy::new);
    }
    
    public static RecommendationStrategy createStrategy(String nutrient) {
        Supplier<RecommendationStrategy> creator = strategyCreators.get(nutrient.toLowerCase());
        return creator != null ? creator.get() : null;
    }
    
    public static void registerStrategy(String nutrient, Supplier<RecommendationStrategy> creator) {
        strategyCreators.put(nutrient.toLowerCase(), creator);
    }
} 