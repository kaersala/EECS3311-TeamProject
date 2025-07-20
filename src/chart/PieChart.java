package chart;

import java.util.Map;

public class PieChart implements Chart {
    private String title;
    private Map<String, Double> data;

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
        // Implementation for rendering pie chart
        System.out.println("Rendering Pie Chart: " + title);
    }
}
