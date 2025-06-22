package controller;

import adapter.JsonAdapter;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealBuilder;
import model.meal.MealType;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class MealLoggerController implements IMealLogger {
    private MealDAO mealDAO = new MealDAO();  // Assumes you’ve implemented this DAO

    @Override
    public void logMeal(Meal meal) {
        //mealDAO.saveMeal(meal);  // Save to database
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
        List<String> mealJsons = adapter.loadAllJsonStrings("meal"); // 读取 meal.json 中的所有 JSON 字符串
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

