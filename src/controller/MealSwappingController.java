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
import dao.Implementations.FoodDAO;
import dao.interfaces.IFoodDAO;

import java.util.*;

public class MealSwappingController {
    private final UserProfileController userProfileController;
    private final MealLoggerController mealController;
    private final NutrientChangesCalculator changesCalculator;
    private final IFoodDAO foodDAO;

    public MealSwappingController(UserProfileController userProfileController,
                                  MealLoggerController mealController) {
        this.userProfileController = userProfileController;
        this.mealController = mealController;
        this.changesCalculator = new NutrientChangesCalculator();
        this.foodDAO = new FoodDAO(null); // Use null connection for demo
    }

    public List<SwapSuggestion> generateSwapSuggestions(Meal meal, List<Goal> goals) {
        // Load food item database from MySQL using DAO
        List<FoodItem> foodList = foodDAO.loadFoods();
        Map<Integer, FoodItem> foodDatabase = new HashMap<>();
        for (FoodItem item : foodList) {
            foodDatabase.put(item.getFoodID(), item);
        }

        // Generate swap suggestions
        SwapEngine swapEngine = new SwapEngine();
        return swapEngine.generateSwaps(goals, meal.getIngredients(), foodDatabase);
    }

    public Map<String, Double> getNutrientChange(Meal original, Meal swapped) {
        return changesCalculator.computeDifferences(original, swapped);
    }

    public UserProfile getCurrentUser() {
        return userProfileController.getCurrentProfile();
    }

    public List<Meal> getLoggedMeals() {
        return mealController.loadMeals();
    }
}
