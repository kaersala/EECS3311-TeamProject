// === MealController.java ===
package controller;


import dao.Implementations.MealDAO;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealBuilder;
import model.meal.MealType;

import java.time.LocalDate;
import java.util.List;

public class MealLoggerController implements IMealLogger {
    private final MealDAO mealDAO = new MealDAOImpl();

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
        return mealDAO.getMealsForUser(userId);
    }

    @Override
    public List<Meal> loadMeals() {
        // Placeholder: in a real system, userId should be passed dynamically
        int exampleUserId = 1;
        return mealDAO.getMealsForUser(exampleUserId);
    }

    @Override
    public void updateMeal(Meal meal) {
        mealDAO.updateMeal(meal);
    }
} 
