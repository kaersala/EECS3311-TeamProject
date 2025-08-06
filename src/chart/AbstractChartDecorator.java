package chart;

public abstract class AbstractChartDecorator extends ChartDecorator {
    
    public AbstractChartDecorator(Chart chart) {
        super(chart);
    }
    
    @Override
    public void render() {
        addDecoration();
        super.render();
    }
    
    protected abstract void addDecoration();
} 