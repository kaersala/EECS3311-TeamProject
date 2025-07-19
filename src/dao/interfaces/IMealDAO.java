package dao.interfaces;

import model.meal.Meal;
import java.util.List;

public interface IMealDAO {
    List<Meal> getMealsByUserId(int userId);
    Meal getMealById(int mealId);
    void saveMeal(Meal meal);
}
