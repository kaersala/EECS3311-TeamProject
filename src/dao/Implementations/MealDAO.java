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
        List<Meal> allMeals = new ArrayList<>();
        // For now, this is a placeholder that returns null
        return null;
    }

    @Override
    public void saveMeal(Meal meal) {
        adapter.saveMeal(meal);
    }

    @Override
    public void updateMeal(Meal meal) {
        adapter.updateMeal(meal);
    }
}
