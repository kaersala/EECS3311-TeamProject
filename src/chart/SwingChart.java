package chart;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class SwingChart implements Chart {
    private String title;
    private Map<String, Double> data;
    private JPanel chartPanel;

    public SwingChart() {
        this.chartPanel = new JPanel();
    }

    public SwingChart(String title) {
        this.title = title;
        this.chartPanel = new JPanel();
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setData(Map<String, Double> data) {
        this.data = data;
        updateChartPanel();
    }

    @Override
    public void render() {
        updateChartPanel();
    }

    private void updateChartPanel() {
        chartPanel.removeAll();
        chartPanel.setLayout(new BorderLayout());
        
        // Add title
        if (title != null) {
            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            chartPanel.add(titleLabel, BorderLayout.NORTH);
        }
        
        // Add data display
        if (data != null && !data.isEmpty()) {
            JTextArea dataArea = new JTextArea();
            dataArea.setEditable(false);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            dataArea.setText(sb.toString());
            chartPanel.add(new JScrollPane(dataArea), BorderLayout.CENTER);
        }
        
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    public JPanel getChartPanel() {
        return chartPanel;
    }
}
