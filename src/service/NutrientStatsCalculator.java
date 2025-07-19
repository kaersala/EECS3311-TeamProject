package service;

import model.meal.Meal;

import java.util.*;
import java.util.stream.Collectors;

/**
 * NutrientStatsCalculator provides utilities to:
 * - Aggregate nutrients from meals
 * - Extract top contributing nutrients
 * - Calculate percentage contributions of nutrients
 */
public class NutrientStatsCalculator {

    /**
     * Aggregates nutrient totals across a list of meals.
     *
     * @param meals List of Meal objects
     * @return Map of nutrient name to total intake amount
     */
    public Map<String, Double> aggregateDailyIntake(List<Meal> meals) {
        Map<String, Double> totals = new HashMap<>();
        for (Meal meal : meals) {
            Map<String, Double> nutrients = meal.getNutrients();
            for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
                totals.put(entry.getKey(), totals.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
            }
        }
        return totals;
    }

    /**
     * Returns the top N nutrients based on their intake amounts.
     *
     * @param data Map of nutrient name to intake amount
     * @param n number of top nutrients to return
     * @return List of nutrient name-value pairs sorted by value descending
     */
    public List<Map.Entry<String, Double>> getTopNutrients(Map<String, Double> data, int n) {
        return data.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Computes the percent contribution of each nutrient to the total.
     *
     * @param data Map of nutrient name to intake amount
     * @return Map of nutrient name to percent contribution (0–100)
     */
    public Map<String, Double> computePercentContributions(Map<String, Double> data) {
        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            if (total > 0) {
                result.put(entry.getKey(), (entry.getValue() / total) * 100);
            } else {
                result.put(entry.getKey(), 0.0);
            }
        }
        return result;
    }
}
