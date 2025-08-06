package chart;

public class HighlightDecorator extends AbstractChartDecorator {

    public HighlightDecorator(Chart chart) {
        super(chart);
    }

    @Override
    protected void addDecoration() {
        System.out.println("🌟 Highlighting top nutrients");
    }
}
