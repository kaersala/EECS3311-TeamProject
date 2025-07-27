package dao.adapter;

import java.sql.Connection;
import java.util.List;
import model.*;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.user.UserProfile;

public interface DatabaseAdapter {
    Connection connect();
    Connection getConnection();

    void saveMeal(Meal meal);
    void updateMeal(Meal meal);
    void deleteMeal(int mealId);
    void deleteMealsByDate(int userId, String date);
    List<Meal> loadMeals(int userId);
    void updateIngredientQuantity(int mealId, int foodId, double newQuantity);

    void saveProfile(UserProfile profile);
    List<UserProfile> loadProfiles();

    List<IngredientEntry> loadIngredients();
    List<FoodItem> loadFoods();
    List<Nutrient> loadNutrients();
}