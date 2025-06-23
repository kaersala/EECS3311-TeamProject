package chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;

public class SwingChart implements Chart {
    private final JFreeChart chart;

    public SwingChart(JFreeChart chart) {
        this.chart = chart;
    }

    @Override
    public void display() {
        JFrame frame = new JFrame("Chart Display");
        frame.setContentPane(new ChartPanel(chart));
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
}
