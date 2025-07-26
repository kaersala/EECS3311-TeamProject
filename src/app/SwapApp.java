package app;

import backend.SwapEngine;
import model.*;
import model.meal.IngredientEntry;
import model.meal.Meal;

import java.util.*;

public class SwapApp {
    public static void main(String[] args) {
        // Create a mock food database
        Map<Integer, FoodItem> foodDatabase = new HashMap<>();
        foodDatabase.put(1, new FoodItem(1, "Beef", 250, Map.of("Calories", 250.0), "Meat"));
        foodDatabase.put(2, new FoodItem(2, "Chicken", 150, Map.of("Calories", 150.0), "Meat"));

        // Create a meal with Beef
        IngredientEntry beefEntry = new IngredientEntry(1, 100); // 100g
        Meal meal = new Meal(1, 1, java.time.LocalDate.now(), null, List.of(beefEntry));

        // Set a goal to decrease calories
        Goal calorieGoal = new Goal("Calories", "decrease", 200, "low");
        List<Goal> goals = List.of(calorieGoal);

        // Generate swap suggestions
        SwapEngine swapEngine = new SwapEngine();
        List<SwapSuggestion> suggestions = swapEngine.generateSwaps(goals, meal.getIngredients(), foodDatabase);

        // Print swap suggestions
        if (!suggestions.isEmpty()) {
            System.out.println("Swap suggestion: " + suggestions.get(0));
        } else {
            System.out.println("No swap suggestions generated.");
        }
    }
} 