package chart;

import java.util.Map;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ChartFactory;

public class NutrientChartFactory {

    public Chart createNutrientPieChart(Map<String, Double> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Nutrient Breakdown",
                dataset,
                true, true, false
        );

        return new SwingChart(pieChart);
    }
}
