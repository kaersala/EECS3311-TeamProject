package chart;

import java.util.Map;

/**
 * Factory for creating Canada Food Guide-related charts:
 * - Plate chart (food group percentages)
 * - Adherence bar chart (compliance score)
 */
public class CFGChartFactory {

    /**
     * Creates a plate chart showing food group distribution
     */
    public Chart createPlateChart(Map<String, Double> data) {
        SwingChart chart = new SwingChart("Canada Food Guide Plate");
        chart.setData(data);
        return chart;
    }

    /**
     * Creates an adherence bar chart showing CFG compliance score
     */
    public Chart createAdherenceBarChart(double score) {
        Map<String, Double> scoreData = Map.of("Your Score", score);
        SwingChart chart = new SwingChart("CFG Compliance Score");
        chart.setData(scoreData);
        return chart;
    }
}
