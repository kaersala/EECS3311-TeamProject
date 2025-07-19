package chart;

public class InteractiveChartDecorator extends ChartDecorator {

    public InteractiveChartDecorator(Chart chart) {
        super(chart);
    }

    @Override
    public void render() {
        System.out.println("🖱️ Interactive features activated");
        super.render();
    }
}
