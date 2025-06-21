package dao.interfaces;

import model.user.FoodItem;
import java.util.List;

public interface IFoodDAO {
    List<FoodItem> loadFoods();
    FoodItem getFoodById(int foodId);
}
