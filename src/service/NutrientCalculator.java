package service;

import model.FoodItem;
import model.meal.IngredientEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutrientCalculator {
    
    public static Map<String, Double> calculateMealNutrients(List<IngredientEntry> meal, Map<Integer, FoodItem> foodDatabase) {
        Map<String, Double> totalNutrients = new HashMap<>();
        
        for (IngredientEntry entry : meal) {
            FoodItem food = foodDatabase.get(entry.getFoodID());
            if (food != null) {
                double quantity = entry.getQuantity();
                
                // Add calories
                totalNutrients.merge("calories", food.getCalories() * quantity / 100.0, Double::sum);
                
                // Add other nutrients
                for (Map.Entry<String, Double> nutrient : food.getNutrients().entrySet()) {
                    totalNutrients.merge(nutrient.getKey().toLowerCase(), 
                                       nutrient.getValue() * quantity / 100.0, Double::sum);
                }
            }
        }
        
        return totalNutrients;
    }
} 