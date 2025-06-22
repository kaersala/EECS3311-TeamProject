package controller;

import backend.GoalChecker;
import backend.NutritionAnalyzer;
import backend.SwapEngine;
import model.FoodItem;
import model.Goal;
import model.SwapSuggestion;
import model.meal.Meal;
import model.user.UserProfile;
import service.NutrientChangesCalculator;
import adapter.JsonAdapter;

import java.util.*;

public class MealSwappingController {
    private final UserProfileController userProfileController;
    private final MealLoggerController mealLoggerController;
    private final NutrientChangesCalculator changesCalculator;
    private final JsonAdapter adapter;

    public MealSwappingController(UserProfileController userProfileController,
                                  MealLoggerController mealLoggerController) {
        this.userProfileController = userProfileController;
        this.mealLoggerController = mealLoggerController;
        this.changesCalculator = new NutrientChangesCalculator();
        this.adapter = new JsonAdapter();
    }

    public List<SwapSuggestion> generateSwapSuggestions(Meal meal, List<Goal> goals) {
        // 1. Load food item database from JSON or static data
        List<FoodItem> foodList = adapter.loadAllFoodItems("fooditems.json");
        Map<Integer, FoodItem> foodDatabase = new HashMap<>();
        for (FoodItem item : foodList) {
            foodDatabase.put(item.getFoodId(), item);
        }

        // 2. Analyze nutrients in the current meal
        NutritionAnalyzer analyzer = new NutritionAnalyzer(foodDatabase);
        Map<String, Double> currentNutrients = analyzer.analyzeMeal(meal);

        // 3. Compute gaps based on goals
        GoalChecker checker = new GoalChecker();
        Map<String, Double> gaps = checker.evaluate(currentNutrients, goals);

        // 4. Generate swap suggestions
        SwapEngine swapEngine = new SwapEngine(foodDatabase);
        return swapEngine.suggestSwaps(meal, goals, gaps);
    }

    public Map<String, Double> getNutrientChange(Meal original, Meal swapped) {
        return changesCalculator.computeDifferences(original, swapped);
    }

    public UserProfile getCurrentUser() {
        return userProfileController.getCurrentProfile();
    }

    public List<Meal> getLoggedMeals() {
        return mealLoggerController.loadMeals();
    }
}
