package service;

import model.user.UserProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * CFGComparisonEngine compares daily nutrient intake against CFG guidelines.
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
     * Gets CFG recommended daily nutrient intakes for the given profile.
     * This can be extended later to differentiate based on age, sex, etc.
     */
    private Map<String, Double> getCFGRecommendedIntake(UserProfile profile) {
        Map<String, Double> targets = new HashMap<>();

        // Example fixed targets for demonstration (can be adjusted per profile later)
        targets.put("Calories", 2000.0);
        targets.put("Protein", 50.0);    // grams
        targets.put("Fiber", 25.0);      // grams
        targets.put("Sodium", 1500.0);   // mg
        targets.put("Fat", 70.0);        // grams

        return targets;
    }
}
