package chart;

public class AnnotationChartDecorator extends ChartDecorator {
    public AnnotationChartDecorator(Chart chart) {
        super(chart);
    }

    @Override
    public void display() {
        System.out.println("[Annotation] Adding annotations to chart...");
        chart.display();
    }
}
