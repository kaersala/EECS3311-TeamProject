package dao.interfaces;

import model.user.Nutrient;
import java.util.List;

public interface INutrientDAO {
    List<Nutrient> loadAllNutrients();
    Nutrient getNutrientById(int nutrientId);
}
