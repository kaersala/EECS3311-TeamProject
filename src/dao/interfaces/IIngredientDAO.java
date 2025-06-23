package dao.interfaces;

import model.IngredientEntry;
import java.util.List;

public interface IIngredientDAO {
    List<IngredientEntry> loadIngredients();
    IngredientEntry getIngredientByFoodId(int foodId);
}
