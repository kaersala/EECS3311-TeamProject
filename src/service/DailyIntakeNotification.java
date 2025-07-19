package service;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates daily intake notifications based on nutrient thresholds.
 */
public class DailyIntakeNotification {

    /**
     * Checks if any nutrient intake is critically low or high and returns warnings.
     *
     * @param actualIntake map of nutrient name to intake amount
     * @return map of nutrient name to message if warning is needed, otherwise not included
     */
    public Map<String, String> generateWarnings(Map<String, Double> actualIntake) {
        Map<String, String> notifications = new HashMap<>();
        Map<String, Double> recommended = getGeneralRecommendedIntake();

        for (Map.Entry<String, Double> entry : recommended.entrySet()) {
            String nutrient = entry.getKey();
            double recommendedAmount = entry.getValue();
            double actual = actualIntake.getOrDefault(nutrient, 0.0);

            if (actual < 0.7 * recommendedAmount) {
                notifications.put(nutrient, "Your intake is too low for " + nutrient + ". Consider increasing it.");
            } else if (actual > 1.5 * recommendedAmount) {
                notifications.put(nutrient, "Your intake is too high for " + nutrient + ". Consider reducing it.");
            }
        }

        return notifications;
    }

    /**
     * General recommended daily intake values (example estimates).
     * Can be customized further based on user's profile data.
     */
    private Map<String, Double> getGeneralRecommendedIntake() {
        Map<String, Double> targets = new HashMap<>();
        targets.put("Calories", 2000.0);
        targets.put("Protein", 50.0);    // grams
        targets.put("Fiber", 25.0);      // grams
        targets.put("Sodium", 1500.0);   // mg
        targets.put("Fat", 70.0);        // grams
        return targets;
    }
}
