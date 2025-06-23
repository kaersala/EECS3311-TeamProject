package service;

import model.meal.Meal;
import java.util.*;
import java.util.stream.Collectors;

public class NutrientStatsCalculator {

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

    public List<Map.Entry<String, Double>> getTopNutrients(Map<String, Double> data, int n) {
        return data.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .collect(Collectors.toList());
    }

    public Map<String, Double> computePercentContributions(Map<String, Double> data) {
        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            result.put(entry.getKey(), (entry.getValue() / total) * 100);
        }
        return result;
    }
}
