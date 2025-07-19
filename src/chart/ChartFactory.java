package chart;

import java.util.Map;

public abstract class ChartFactory {
    public abstract Chart createChart(Map<String, Double> data);
}
