package dao.interfaces;

import model.meal.IngredientEntry;
import java.util.List;

public interface IIngredientDAO {
    List<IngredientEntry> loadIngredients();
    IngredientEntry getIngredientByFoodId(int foodId);
}
