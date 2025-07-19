package service;

import model.*;
import model.meal.Meal;

import java.util.*;

/**
 * Computes the nutrient difference between two meals.
 */
public class NutrientChangesCalculator {

    /**
     * Computes the nutrient differences from Meal m1 to Meal m2.
     * Positive value means increase in that nutrient, negative means decrease.
     *
     * @param m1 the original meal
     * @param m2 the modified/swapped meal
     * @return map of nutrient name to change in amount (rounded to 1 decimal)
     */
    public Map<String, Double> computeDifferences(Meal m1, Meal m2) {
        Map<String, Double> nutrients1 = m1.getNutrients();
        Map<String, Double> nutrients2 = m2.getNutrients();
        Map<String, Double> result = new HashMap<>();

        Set<String> allNutrients = new HashSet<>();
        allNutrients.addAll(nutrients1.keySet());
        allNutrients.addAll(nutrients2.keySet());

        for (String nutrient : allNutrients) {
            double val1 = nutrients1.getOrDefault(nutrient, 0.0);
            double val2 = nutrients2.getOrDefault(nutrient, 0.0);
            double diff = val2 - val1;
            result.put(nutrient, Math.round(diff * 10.0) / 10.0);
        }

        return result;
    }
}
