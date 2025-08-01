package controller;

import dao.Implementations.MealDAO;
import dao.interfaces.IMealDAO;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealBuilder;
import model.meal.MealType;

import java.time.LocalDate;
import java.util.List;

import backend.SwapEngine;

public class MealLoggerController implements IMealLogger {
    private final IMealDAO mealDAO = new MealDAO();

    @Override
    public void logMeal(Meal meal) {
        mealDAO.saveMeal(meal);
    }

    @Override
    public Meal buildMeal(int userId, LocalDate date, MealType type, List<IngredientEntry> ingredients) {
        MealBuilder builder = new MealBuilder()
                .setUserId(userId)
                .setDate(date)
                .setType(type);
        ingredients.forEach(builder::addIngredient);
        return builder.build();
    }

    @Override
    public List<Meal> getMealsForUser(int userId) {
        return mealDAO.getMealsByUserId(userId);
    }

    @Override
    public List<Meal> loadMeals() {
        int exampleUserId = 1;
        return mealDAO.getMealsByUserId(exampleUserId);
    }

    @Override
    public void updateMeal(Meal meal) {
        mealDAO.saveMeal(meal);
    }
    
    public SwapEngine getSwapEngine() {
        return new SwapEngine(); // Uses strategies internally
    }

} 
