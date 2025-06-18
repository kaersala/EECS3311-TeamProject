package controller;

import model.Meal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MealLoggerController {
    public List<Meal> getMeals(int userId) {
        List<Meal> meals = new ArrayList<>();
        meals.add(new Meal(new Date(), "Breakfast", 350));
        meals.add(new Meal(new Date(), "Lunch", 600));
        meals.add(new Meal(new Date(), "Snack", 200));
        meals.add(new Meal(new Date(), "Dinner", 700));
        return meals;
    }
}
