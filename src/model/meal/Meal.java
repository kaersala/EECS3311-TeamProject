package model.meal;

import java.time.LocalDate;
import java.util.*;

public class Meal {
    private int mealID;
    private int userID;
    private LocalDate date;
    private MealType type;
    private List<IngredientEntry> ingredients;

    public Meal(int mealID, int userID, LocalDate date, MealType type, List<IngredientEntry> ingredients) {
        this.mealID = mealID;
        this.userID = userID;
        this.date = date;
        this.type = type;
        this.ingredients = ingredients;
    }

    public int getMealID() {
        return mealID;
    }
    
    public MealType getType() {
        return type;
    }

    // Alias method for compatibility
    public MealType getMealType() {
        return type;
    }

    /**
     * Accurate calorie calculation using NutritionAnalyzer and food database
     */
    public double getCalories(Map<Integer, model.FoodItem> foodDatabase) {
        backend.NutritionAnalyzer analyzer = new backend.NutritionAnalyzer(foodDatabase);
        Map<String, Double> nutrients = analyzer.analyzeMeal(this);
        return nutrients.getOrDefault("Calories", 0.0);
    }

    /**
     * Deprecated: Use getCalories(Map<Integer, FoodItem>) instead for accurate calculation
     */
    @Deprecated
    public double getCalories() {
        // placeholder for actual calorie calculation
        return 100.0;
    }

    public Map<String, Double> getNutrients() {
        // placeholder for nutrient computation
        return new HashMap<>();
    }

    public List<IngredientEntry> getIngredients() {
        return ingredients;
    }

    public int getUserID() {
        return userID;
    }

    public LocalDate getDate() {
        return date;
    }
}

