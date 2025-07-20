package controller;

import backend.NutritionAnalyzer;
import model.FoodItem;
import model.meal.Meal;
import model.user.UserProfile;
import service.CFGComparisonEngine;
import dao.Implementations.MealDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyIntakeAndCFGAnalysisController {
    private final MealDAO mealDAO;
    private final Map<Integer, FoodItem> foodDatabase;

    public DailyIntakeAndCFGAnalysisController(MealDAO mealDAO, Map<Integer, FoodItem> foodDatabase) {
        this.mealDAO = mealDAO;
        this.foodDatabase = foodDatabase;
    }

    public Map<String, Double> getDailyNutrientIntake(int userId) {
        List<Meal> meals = mealDAO.getMealsByUserId(userId);
        NutritionAnalyzer analyzer = new NutritionAnalyzer(foodDatabase);
        Map<String, Double> dailyTotals = new HashMap<>();

        for (Meal meal : meals) {
            Map<String, Double> nutrients = analyzer.analyzeMeal(meal);
            for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
                dailyTotals.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }

        return dailyTotals;
    }

    public Map<String, Boolean> analyzeCFGCompliance(UserProfile profile) {
        Map<String, Double> dailyIntake = getDailyNutrientIntake(profile.getUserID());
        CFGComparisonEngine engine = new CFGComparisonEngine();
        return engine.compareToCFG(profile, dailyIntake);
    }
}
