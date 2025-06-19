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

    public MealType getType() {
        return type;
    }

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

