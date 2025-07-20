package chart;

import java.util.Map;

public class SwapEffectChartFactory {
    public Chart createSwapEffectChart(Map<String, Double> before, Map<String, Double> after) {
        // Create a simple chart showing before/after comparison
        SwingChart chart = new SwingChart("Swap Effects");
        
        // Combine before and after data for display
        Map<String, Double> combinedData = new java.util.HashMap<>();
        for (String key : before.keySet()) {
            combinedData.put("Before " + key, before.get(key));
            if (after.containsKey(key)) {
                combinedData.put("After " + key, after.get(key));
            }
        }
        
        chart.setData(combinedData);
        return chart;
    }
}
