package backend;

import model.FoodItem;
import model.meal.Meal;
import model.meal.IngredientEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NutritionAnalyzer calculates total nutrient values from a Meal
 * based on food nutrient profiles and ingredient quantities.
 */
public class NutritionAnalyzer {

    private Map<Integer, FoodItem> foodDatabase;

    /**
     * @param foodDatabase Map of foodID to FoodItem
     */
    public NutritionAnalyzer(Map<Integer, FoodItem> foodDatabase) {
        this.foodDatabase = foodDatabase;
    }

    /**
     * Calculates total nutrients for the given meal.
     * @param meal the Meal to analyze
     * @return a map of nutrient name to total amount in the meal
     */
    public Map<String, Double> analyzeMeal(Meal meal) {
        Map<String, Double> totalNutrients = new HashMap<>();

        List<IngredientEntry> ingredients = meal.getIngredients();

        for (IngredientEntry entry : ingredients) {
            FoodItem food = foodDatabase.get(entry.getFoodID());
            if (food == null) continue;

            double quantityFactor = entry.getQuantity() / 100.0; // nutrients per 100g

            for (Map.Entry<String, Double> nutrient : food.getNutrients().entrySet()) {
                String nutrientName = nutrient.getKey();
                double amount = nutrient.getValue() * quantityFactor;

                totalNutrients.merge(nutrientName, amount, Double::sum);
            }
        }

        return totalNutrients;
    }
}