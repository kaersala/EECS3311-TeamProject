package controller;

import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealType;

import java.time.LocalDate;
import java.util.List;

public interface IMealLogger {
    void logMeal(Meal meal);

    Meal buildMeal(int userId, LocalDate date, MealType type, List<IngredientEntry> ingredients);

    List<Meal> getMealsForUser(int userId);

    List<Meal> loadMeals();

    void updateMeal(Meal meal);
}
