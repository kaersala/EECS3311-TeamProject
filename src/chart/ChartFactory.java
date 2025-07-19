package chart;

import java.util.Map;

public class ChartFactory {
    public static Chart createChart(String type, String title, Map<String, Double> data) {
        Chart chart;

        if ("pie".equalsIgnoreCase(type)) {
            chart = new PieChart();
        } else if ("bar".equalsIgnoreCase(type)) {
            chart = new BarChart();
        } else {
            throw new IllegalArgumentException("Unsupported chart type: " + type);
        }

        chart.setTitle(title);
        chart.setData(data);
        return chart;
    }
}
