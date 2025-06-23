import chart.Chart;
import chart.NutrientChartFactory;

import java.util.HashMap;
import java.util.Map;

public class ChartTest {
    public static void main(String[] args) {
        // Create some dummy nutrient data
        Map<String, Double> nutrientData = new HashMap<>();
        nutrientData.put("Protein", 50.0);
        nutrientData.put("Carbohydrates", 100.0);
        nutrientData.put("Fats", 30.0);
        nutrientData.put("Fiber", 10.0);

        // Create a pie chart using the factory
        NutrientChartFactory factory = new NutrientChartFactory();
        Chart chart = factory.createNutrientPieChart(nutrientData);

        // Display the chart in a Swing window
        chart.display();
    }
}
