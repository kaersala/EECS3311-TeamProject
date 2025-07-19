package chart;

public class TooltopDecorator extends ChartDecorator {
    public TooltopDecorator(Chart chart) {
        super(chart);
    }

    @Override
    public void display() {
        System.out.println("[Tooltip] Adding tooltips to chart...");
        chart.display();
    }
}