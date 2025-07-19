package dao.Implementations;

import dao.adapter.DatabaseAdapter;
import dao.adapter.MySQLAdapter;
import dao.interfaces.IMealDAO;
import model.meal.Meal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MealDAO implements IMealDAO {

    private final DatabaseAdapter adapter;

    public MealDAO() {
        this.adapter = new MySQLAdapter();
        this.adapter.connect();
    }

    @Override
    public List<Meal> getMealsByUserId(int userId) {
        return adapter.loadMeals(userId);
    }

    @Override
    public Meal getMealById(int mealId) {
        // For now, we'll load all meals for all users and filter by meal ID
        // This is not efficient but works as a temporary solution
        // A better approach would be to add a method to DatabaseAdapter interface
        List<Meal> allMeals = new ArrayList<>();
        // We need to implement a method to get all meals or modify the interface
        // For now, this is a placeholder that returns null
        return null;
    }

    @Override
    public void saveMeal(Meal meal) {
        adapter.saveMeal(meal);
    }
}
