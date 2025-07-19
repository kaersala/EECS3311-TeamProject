package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Map;

/**
 * Factory for creating Canada Food Guide-related charts:
 * - Plate chart (food group percentages)
 * - Adherence bar chart (compliance score)
 */
public class CFGChartFactory {

    
    public Chart createPlateChart(Map<String, Double> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Canada Food Guide Plate",
                dataset,
                true,  // include legend
                true,  // tooltips
                false  // URLs
        );

        return new SwingChart(pieChart);
    }

   
    public Chart createAdherenceBarChart(double score) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(score, "Adherence", "Your Score");

        JFreeChart barChart = ChartFactory.createBarChart(
                "CFG Compliance Score",
                "User",
                "Score",
                dataset
        );

        return new SwingChart(barChart);
    }
}
