package app;

import model.*;
import model.meal.IngredientEntry;
import model.meal.Meal;
import service.NutrientChangesCalculator;

import java.util.*;

public class CalcTestApp {
    public static void main(String[] args) {
        // Create a mock food database
        Map<Integer, FoodItem> foodDB = new HashMap<>();
        foodDB.put(1, new FoodItem(1, "Beef", 250, Map.of("Calories", 250.0), "Meat"));
        foodDB.put(2, new FoodItem(2, "Chicken", 150, Map.of("Calories", 150.0), "Meat"));

        // Create two meals: one with Beef, one with Chicken
        IngredientEntry beefEntry = new IngredientEntry(1, 100); 
        IngredientEntry chickenEntry = new IngredientEntry(2, 100); 
        Meal meal1 = new Meal(1, 1, java.time.LocalDate.now(), null, List.of(beefEntry));
        Meal meal2 = new Meal(2, 1, java.time.LocalDate.now(), null, List.of(chickenEntry));

        // Calculate nutrient difference
        NutrientChangesCalculator calc = new NutrientChangesCalculator();
        Map<String, Double> diff = calc.computeDifferences(meal1, meal2);

        // Print the nutrient difference
        System.out.println("Nutrient difference after swap: " + diff);
    }
} 
