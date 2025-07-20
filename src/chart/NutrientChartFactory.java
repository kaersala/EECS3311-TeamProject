package chart;

import java.util.Map;

/**
 * Factory for creating nutrition-related charts
 */
public class NutrientChartFactory {

    /**
     * Creates a bar chart comparing nutrient changes
     */
    public Chart createNutrientComparisonChart(Map<String, Double> nutrientChanges) {
        SwingChart chart = new SwingChart("Nutrient Changes After Swap");
        chart.setData(nutrientChanges);
        return chart;
    }

    /**
     * Creates a pie chart showing daily nutrient distribution
     */
    public Chart createDailyNutrientChart(Map<String, Double> dailyNutrients) {
        SwingChart chart = new SwingChart("Daily Nutrient Distribution");
        chart.setData(dailyNutrients);
        return chart;
    }

    /**
     * Creates a line chart showing nutrient trends over time
     */
    public Chart createNutrientTrendChart(Map<String, Map<String, Double>> nutrientTrends) {
        // Convert nested map to flat map for display
        Map<String, Double> flatData = new java.util.HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : nutrientTrends.entrySet()) {
            String date = entry.getKey();
            Map<String, Double> nutrients = entry.getValue();
            for (Map.Entry<String, Double> nutrient : nutrients.entrySet()) {
                flatData.put(date + " - " + nutrient.getKey(), nutrient.getValue());
            }
        }
        
        SwingChart chart = new SwingChart("Nutrient Trends Over Time");
        chart.setData(flatData);
        return chart;
    }
}
