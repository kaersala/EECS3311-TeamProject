package service;

import model.user.UserProfile;
import model.meal.Meal;
import model.FoodItem;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * CFGComparisonEngine compares daily nutrient intake against CFG guidelines.
 * Supports both CFG 2007 and CFG 2019 standards.
 */
public class CFGComparisonEngine {

    /**
     * Compares actual intake with CFG recommended values.
     *
     * @param profile the user profile
     * @param actualIntake map of nutrient name to daily intake amount
     * @return map of nutrient name to Boolean (true = meets CFG target, false = does not)
     */
    public Map<String, Boolean> compareToCFG(UserProfile profile, Map<String, Double> actualIntake) {
        Map<String, Double> cfgTargets = getCFGRecommendedIntake(profile);
        Map<String, Boolean> result = new HashMap<>();

        for (Map.Entry<String, Double> entry : cfgTargets.entrySet()) {
            String nutrient = entry.getKey();
            double recommended = entry.getValue();
            double actual = actualIntake.getOrDefault(nutrient, 0.0);

            // Consider target met if intake is within ±10% of recommendation
            result.put(nutrient, actual >= 0.9 * recommended && actual <= 1.1 * recommended);
        }

        return result;
    }

    /**
     * Calculates food group distribution for CFG plate visualization
     */
    public Map<String, Double> calculateFoodGroupDistribution(List<Meal> meals, Map<Integer, FoodItem> foodDatabase) {
        Map<String, Double> groupTotals = new HashMap<>();
        double totalCalories = 0.0;

        for (Meal meal : meals) {
            for (var entry : meal.getIngredients()) {
                FoodItem food = foodDatabase.get(entry.getFoodID());
                if (food != null) {
                    double calories = food.getCalories() * (entry.getQuantity() / 100.0);
                    String group = food.getFoodGroup();
                    
                    groupTotals.merge(group, calories, Double::sum);
                    totalCalories += calories;
                }
            }
        }

        // Convert to percentages
        Map<String, Double> percentages = new HashMap<>();
        for (Map.Entry<String, Double> entry : groupTotals.entrySet()) {
            percentages.put(entry.getKey(), (entry.getValue() / totalCalories) * 100.0);
        }

        return percentages;
    }

    /**
     * Gets CFG recommended daily nutrient intakes for the given profile.
     * This can be extended later to differentiate based on age, sex, etc.
     */
    private Map<String, Double> getCFGRecommendedIntake(UserProfile profile) {
        Map<String, Double> targets = new HashMap<>();

        // CFG 2019 recommendations (simplified)
        targets.put("Calories", 2000.0);
        targets.put("Protein", 50.0);    // grams
        targets.put("Fiber", 25.0);      // grams
        targets.put("Sodium", 1500.0);   // mg
        targets.put("Fat", 70.0);        // grams
        targets.put("Carbohydrates", 275.0); // grams
        targets.put("Vitamin C", 90.0);  // mg
        targets.put("Calcium", 1000.0);  // mg
        targets.put("Iron", 18.0);       // mg

        return targets;
    }

    /**
     * Gets CFG 2019 plate recommendations
     */
    public Map<String, Double> getCFGPlateRecommendations() {
        Map<String, Double> plate = new HashMap<>();
        
        // CFG 2019 plate model
        plate.put("Vegetables and Fruits", 50.0);  // 50% of plate
        plate.put("Protein Foods", 25.0);          // 25% of plate
        plate.put("Whole Grain Foods", 25.0);      // 25% of plate
        
        return plate;
    }

    /**
     * Calculates CFG compliance score (0-100)
     */
    public double calculateComplianceScore(Map<String, Double> actualDistribution, Map<String, Double> recommendedDistribution) {
        double totalScore = 0.0;
        int count = 0;

        for (Map.Entry<String, Double> entry : recommendedDistribution.entrySet()) {
            String group = entry.getKey();
            double recommended = entry.getValue();
            double actual = actualDistribution.getOrDefault(group, 0.0);
            
            // Calculate how close actual is to recommended (0-100 score)
            double score = Math.max(0, 100 - Math.abs(actual - recommended) * 2);
            totalScore += score;
            count++;
        }

        return count > 0 ? totalScore / count : 0.0;
    }
}
