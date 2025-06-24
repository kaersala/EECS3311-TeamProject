package controller;

import adapter.JsonAdapter;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealBuilder;
import model.meal.MealType;
import dao.Implementations.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class MealLoggerController implements IMealLogger {
    private MealDAO mealDAO = new MealDAO();  // Assumes you’ve implemented this DAO

    @Override
    public void logMeal(Meal meal) {
        JsonAdapter adapter = new JsonAdapter();
    
        List<String> mealJsons = adapter.loadAllJsonStrings("meal");
        List<Meal> allMeals = new ArrayList<>();
        for (String json : mealJsons) {
            Meal m = adapter.deserializeMeal(json);
            if (m != null) {
                allMeals.add(m);
            }
        }
    
        allMeals.add(meal);
    
        adapter.saveMeals(allMeals, "meal.json");
    }

    @Override
    public Meal buildMeal(int userId, LocalDate date, MealType type, List<IngredientEntry> ingredients) {
        MealBuilder builder = new MealBuilder()
                .setUserId(userId)
                .setDate(date)
                .setType(type);  // assuming string input

        ingredients.forEach(builder::addIngredient);

        return builder.build();
    }

    @Override
    public List<Meal> loadMeals() {
        JsonAdapter adapter = new JsonAdapter();
        List<String> mealJsons = adapter.loadAllJsonStrings("meal");
        List<Meal> meals = new ArrayList<>();
    
        for (String json : mealJsons) {
            Meal meal = adapter.deserializeMeal(json);
            if (meal != null) {
                meals.add(meal);
            }
        }
    
        return meals;
    }
}

