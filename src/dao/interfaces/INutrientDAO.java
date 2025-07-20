package dao.interfaces;

import model.Nutrient;
import java.util.List;

public interface INutrientDAO {
    List<Nutrient> loadAllNutrients();
    Nutrient getNutrientById(int nutrientId);
}
