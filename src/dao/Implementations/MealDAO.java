package dao.Implementations;

import dao.adapter.DatabaseAdapter;
import dao.adapter.MySQLAdapter;
import dao.adapter.DatabaseManager;
import dao.interfaces.IMealDAO;
import model.meal.Meal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MealDAO implements IMealDAO {

    private final DatabaseAdapter adapter;

    public MealDAO() {
        this.adapter = DatabaseManager.getAdapter();
    }

    @Override
    public List<Meal> getMealsByUserId(int userId) {
        return adapter.loadMeals(userId);
    }

    @Override
    public Meal getMealById(int mealId) {
        List<Meal> allMeals = new ArrayList<>();
        
        // Try to find the meal by searching through all users
        for (int userId = 1; userId <= 10; userId++) {
            List<Meal> userMeals = getMealsByUserId(userId);
            if (userMeals != null) {
                for (Meal meal : userMeals) {
                    if (meal.getMealID() == mealId) {
                        return meal;
                    }
                }
            }
        }
        
        System.err.println("Meal with ID " + mealId + " not found");
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
    
    @Override
    public void deleteMeal(int mealId) {
        adapter.deleteMeal(mealId);
    }
    
    @Override
    public void deleteMealsByDate(int userId, String date) {
        adapter.deleteMealsByDate(userId, date);
    }
}