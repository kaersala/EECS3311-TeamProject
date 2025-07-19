package chart;

import java.util.Map;

public class PieChart extends Chart {
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
        // You can add JFreeChart rendering logic here later
        System.out.println("Rendering Pie Chart: " + title);
        for (String key : data.keySet()) {
            System.out.println(key + ": " + data.get(key));
        }
    }
}
