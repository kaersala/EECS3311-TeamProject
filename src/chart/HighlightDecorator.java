package chart;

public class HighlightDecorator extends ChartDecorator {
    public HighlightDecorator(Chart chart) {
        super(chart);
    }

    @Override
    public void display() {
        System.out.println("[Highlight] Adding highlights to chart...");
        chart.display();
    }
}
