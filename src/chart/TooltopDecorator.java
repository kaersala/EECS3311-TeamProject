package chart;

public class TooltopDecorator extends AbstractChartDecorator {

    public TooltopDecorator(Chart chart) {
        super(chart);
    }

    @Override
    protected void addDecoration() {
        System.out.println("🛈 Tooltop enabled");
    }
}
