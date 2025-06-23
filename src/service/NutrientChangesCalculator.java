package service;

import model.*;
import model.meal.Meal;

import java.util.*;

public class NutrientChangesCalculator {
    public Map<String, Double> computeDifferences(Meal m1, Meal m2) {
        Map<String, Double> nutrients1 = m1.getNutrients();
        Map<String, Double> nutrients2 = m2.getNutrients();
        Map<String, Double> result = new HashMap<>();

        for (String nutrient : nutrients1.keySet()) {
            double diff = nutrients2.getOrDefault(nutrient, 0.0) - nutrients1.get(nutrient);
            result.put(nutrient, Math.round(diff * 10.0) / 10.0);
        }
        return result;
    }
}