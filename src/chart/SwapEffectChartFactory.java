package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Map;

public class SwapEffectChartFactory {
    public Chart createSwapEffectChart(Map<String, Double> before, Map<String, Double> after) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (String key : before.keySet()) {
            dataset.addValue(before.get(key), "Before Swap", key);
            if (after.containsKey(key)) {
                dataset.addValue(after.get(key), "After Swap", key);
            }
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Swap Effects",
                "Nutrient",
                "Amount",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        return new SwingChart(barChart);
    }
}
