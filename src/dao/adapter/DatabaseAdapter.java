package dao.adapter;

import java.util.List;
import model.*;

public interface DatabaseAdapter {
    void connect();

    void saveMeal(Meal meal);
    List<Meal> loadMeals(int userId);

    void saveProfile(UserProfile profile);
    List<UserProfile> loadProfiles();

    List<IngredientEntry> loadIngredients();
    List<FoodItem> loadFoods();
    List<Nutrient> loadNutrients();
}
