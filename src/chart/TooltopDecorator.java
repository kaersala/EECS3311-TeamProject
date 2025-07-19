package chart;

public class TooltopDecorator extends ChartDecorator {

    public TooltopDecorator(Chart chart) {
        super(chart);
    }

    @Override
    public void render() {
        System.out.println("🛈 Tooltop enabled");
        super.render();
    }
}
