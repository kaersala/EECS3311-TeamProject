package chart;

public class AnnotationChartDecorator extends ChartDecorator {

    public AnnotationChartDecorator(Chart chart) {
        super(chart);
    }

    @Override
    public void render() {
        System.out.println("📝 Adding annotations...");
        super.render();
    }
}
