package dao.adapter;

import java.sql.Connection;
import java.util.List;
import model.*;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.user.UserProfile;

public interface DatabaseAdapter {
    Connection connect();

    void saveMeal(Meal meal);
    void updateMeal(Meal meal);
    List<Meal> loadMeals(int userId);

    void saveProfile(UserProfile profile);
    List<UserProfile> loadProfiles();

    List<IngredientEntry> loadIngredients();
    List<FoodItem> loadFoods();
    List<Nutrient> loadNutrients();
}