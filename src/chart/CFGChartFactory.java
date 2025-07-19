package chart;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.data.general.DefaultPieDataset;

import java.util.Map;

public class CFGChartFactory {
    public Chart createPlateChart(Map<String, Double> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Canada Food Guide Plate",
                dataset,
                true, true, false
        );

        return new SwingChart(pieChart);
    }
}