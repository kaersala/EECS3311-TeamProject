package chart;

import java.util.Map;

public interface Chart {
    void setTitle(String title);
    void setData(Map<String, Double> data);
    void render();
}
