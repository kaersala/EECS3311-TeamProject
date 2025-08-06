package chart;

public class InteractiveChartDecorator extends AbstractChartDecorator {

    public InteractiveChartDecorator(Chart chart) {
        super(chart);
    }

    @Override
    protected void addDecoration() {
        System.out.println("🖱️ Interactive features activated");
    }
}
