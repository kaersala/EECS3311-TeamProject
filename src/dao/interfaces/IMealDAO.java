package dao.interfaces;

import model.meal.Meal;
import java.util.List;

public interface IMealDAO {
    List<Meal> getMealsByUserId(int userId);
    Meal getMealById(int mealId);
    void saveMeal(Meal meal);
    void updateMeal(Meal meal);
    void deleteMeal(int mealId);
    void deleteMealsByDate(int userId, String date);
}
