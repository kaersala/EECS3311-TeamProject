package controller;
import dao.Implementations.MealDAO;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealBuilder;
import model.meal.MealType;
import java.time.LocalDate;
import java.util.*;

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

//    @Override
//    public List<Meal> getMealsForUser(int userId) {
//        return mealDAO.getMealsForUser(userId);  // from DAO
//    }
}

