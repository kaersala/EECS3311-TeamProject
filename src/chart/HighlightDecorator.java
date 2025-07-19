package chart;

public class HighlightDecorator extends ChartDecorator {

    public HighlightDecorator(Chart chart) {
        super(chart);
    }

    @Override
    public void render() {
        System.out.println("🌟 Highlighting top nutrients");
        super.render();
    }
}
