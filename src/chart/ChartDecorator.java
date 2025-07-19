package chart;

import java.util.Map;

public abstract class ChartDecorator implements Chart {
    protected final Chart chart;

    public ChartDecorator(Chart chart) {
        this.chart = chart;
    }

    @Override
    public void setTitle(String title) {
        chart.setTitle(title);
    }

    @Override
    public void setData(Map<String, Double> data) {
        chart.setData(data);
    }

    @Override
    public void render() {
        chart.render();
    }
}
