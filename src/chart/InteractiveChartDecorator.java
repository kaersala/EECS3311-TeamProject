package chart;

public class InteractiveChartDecorator extends ChartDecorator {
    public InteractiveChartDecorator(Chart chart) {
        super(chart);
    }

    @Override
    public void display() {
        System.out.println("[Interactive] Adding interactivity to chart...");
        chart.display();
    }
}
