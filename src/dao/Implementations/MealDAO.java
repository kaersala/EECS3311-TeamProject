package dao.implementations;

import adapter.JsonAdapter;
import dao.interfaces.IMealDAO;
import model.meal.Meal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MealDAO implements IMealDAO {

    private final JsonAdapter adapter;

    public MealDAO() {
        this.adapter = new JsonAdapter();
    }

    @Override
    public List<Meal> getMealsByUserId(int userId) {
        List<Meal> allMeals = adapter.loadMeals();
        if (allMeals == null) return new ArrayList<>();
        return allMeals.stream()
                .filter(meal -> meal.getUserID() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public Meal getMealById(int mealId) {
        List<Meal> allMeals = adapter.loadMeals();
        if (allMeals == null) return null;
        return allMeals.stream()
                .filter(meal -> meal.getMealID() == mealId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void saveMeal(Meal meal) {
        List<Meal> allMeals = adapter.loadMeals();
        if (allMeals == null) allMeals = new ArrayList<>();
        allMeals.add(meal);
        adapter.saveMeals(allMeals);
    }
}
