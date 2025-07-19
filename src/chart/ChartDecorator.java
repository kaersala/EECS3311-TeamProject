package chart;

import org.jfree.chart.JFreeChart;

public abstract class ChartDecorator implements Chart {
    protected Chart chart;

    public ChartDecorator(Chart chart) {
        this.chart = chart;
    }

    @Override
    public JFreeChart getChart() {
        return chart.getChart();
    }
}
