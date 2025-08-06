package chart;

import java.util.Map;

public abstract class AbstractChart implements Chart {
    protected String title;
    protected Map<String, Double> data;

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setData(Map<String, Double> data) {
        this.data = data;
    }

    @Override
    public void render() {
        System.out.println("Rendering " + getChartType() + ": " + title);
    }

    protected abstract String getChartType();
} 