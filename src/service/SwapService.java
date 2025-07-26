package service;

import model.*;
import model.meal.*;
import dao.interfaces.IMealDAO;
import dao.Implementations.MealDAO;
import backend.SwapEngine;
import java.util.*;
import java.time.LocalDate;

/**
 * Service class for handling food swap operations
 * Implements the swap functionality as described in the use cases
 */
public class SwapService {
    private SwapEngine swapEngine;
    private IMealDAO mealDAO;
    private Map<Integer, FoodItem> foodDatabase;
    
    public SwapService() {
        this.swapEngine = new SwapEngine();
        this.mealDAO = new MealDAO();
        this.foodDatabase = new HashMap<>(); // In real app, load from database
        initializeMockFoodDatabase();
    }
    
    /**
     * Apply swap suggestions to a specific meal
     * @param mealId The meal to modify
     * @param suggestions List of swap suggestions to apply
     * @return Modified meal with applied swaps
     */
    public Meal applySwapsToMeal(int mealId, List<SwapSuggestion> suggestions) {
        try {
            // Get original meal
            Meal originalMeal = getMealById(mealId);
            if (originalMeal == null) {
                throw new IllegalArgumentException("Meal not found: " + mealId);
            }
            
            // Create new meal with applied swaps
            Meal modifiedMeal = createModifiedMeal(originalMeal, suggestions);
            
            // Update database
            mealDAO.updateMeal(modifiedMeal);
            
            return modifiedMeal;
            
        } catch (Exception e) {
            System.err.println("Error applying swaps: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Apply swaps to multiple meals within a date range
     * @param userId User ID
     * @param startDate Start date for applying swaps
     * @param endDate End date for applying swaps
     * @param suggestions Swap suggestions to apply
     * @return Number of meals successfully modified
     */
    public int applySwapsToDateRange(int userId, LocalDate startDate, LocalDate endDate, 
                                   List<SwapSuggestion> suggestions) {
        int modifiedCount = 0;
        
        try {
            // Get all meals in date range
            List<Meal> mealsInRange = getMealsInDateRange(userId, startDate, endDate);
            
            for (Meal meal : mealsInRange) {
                // Apply swaps to each meal
                Meal modifiedMeal = applySwapsToMeal(meal.getMealID(), suggestions);
                if (modifiedMeal != null) {
                    modifiedCount++;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error applying swaps to date range: " + e.getMessage());
        }
        
        return modifiedCount;
    }
    
    /**
     * Generate swap suggestions for a meal based on user goals
     * @param meal The meal to analyze
     * @param goals User's nutrition goals
     * @return List of swap suggestions
     */
    public List<SwapSuggestion> generateSuggestionsForMeal(Meal meal, List<Goal> goals) {
        if (meal.getIngredients() == null || meal.getIngredients().isEmpty()) {
            return new ArrayList<>();
        }
        
        return swapEngine.generateSwaps(goals, meal.getIngredients(), foodDatabase);
    }
    
    /**
     * Compare nutrition before and after swaps
     * @param originalMeal Original meal
     * @param modifiedMeal Meal with applied swaps
     * @return Nutrition comparison data
     */
    public NutritionComparison compareNutrition(Meal originalMeal, Meal modifiedMeal) {
        Map<String, Double> originalNutrients = calculateNutrients(originalMeal);
        Map<String, Double> modifiedNutrients = calculateNutrients(modifiedMeal);
        
        Map<String, NutrientChange> changes = new HashMap<>();
        
        // Calculate changes for each nutrient
        Set<String> allNutrients = new HashSet<>();
        allNutrients.addAll(originalNutrients.keySet());
        allNutrients.addAll(modifiedNutrients.keySet());
        
        for (String nutrient : allNutrients) {
            double original = originalNutrients.getOrDefault(nutrient, 0.0);
            double modified = modifiedNutrients.getOrDefault(nutrient, 0.0);
            double change = modified - original;
            double percentChange = original != 0 ? (change / original) * 100 : 0;
            
            changes.put(nutrient, new NutrientChange(original, modified, change, percentChange));
        }
        
        return new NutritionComparison(changes);
    }
    
    /**
     * Get cumulative nutrition data for a date range
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Cumulative nutrition data
     */
    public Map<String, Double> getCumulativeNutrition(int userId, LocalDate startDate, LocalDate endDate) {
        List<Meal> meals = getMealsInDateRange(userId, startDate, endDate);
        Map<String, Double> cumulative = new HashMap<>();
        
        for (Meal meal : meals) {
            Map<String, Double> mealNutrients = calculateNutrients(meal);
            for (Map.Entry<String, Double> entry : mealNutrients.entrySet()) {
                cumulative.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        
        return cumulative;
    }
    
    // Helper methods
    private Meal getMealById(int mealId) {
        // Mock implementation - in real app, use DAO
        return new Meal(mealId, 1, LocalDate.now(), MealType.LUNCH, 
                       Arrays.asList(new IngredientEntry(1, 100)));
    }
    
    private List<Meal> getMealsInDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        // Mock implementation - in real app, use DAO
        return new ArrayList<>();
    }
    
    private Meal createModifiedMeal(Meal originalMeal, List<SwapSuggestion> suggestions) {
        List<IngredientEntry> modifiedIngredients = new ArrayList<>(originalMeal.getIngredients());
        
        // Apply swaps to ingredients
        for (SwapSuggestion suggestion : suggestions) {
            for (int i = 0; i < modifiedIngredients.size(); i++) {
                IngredientEntry ingredient = modifiedIngredients.get(i);
                if (ingredient.getFoodID() == suggestion.getOriginal().getFoodID() &&
                    ingredient.getQuantity() == suggestion.getOriginal().getQuantity()) {
                    modifiedIngredients.set(i, suggestion.getReplacement());
                    break;
                }
            }
        }
        
        return new Meal(originalMeal.getMealID(), originalMeal.getUserID(), 
                       originalMeal.getDate(), originalMeal.getType(), modifiedIngredients);
    }
    
    private Map<String, Double> calculateNutrients(Meal meal) {
        Map<String, Double> nutrients = new HashMap<>();
        
        if (meal.getIngredients() == null) {
            return nutrients;
        }
        
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        double totalFiber = 0;
        
        for (IngredientEntry ingredient : meal.getIngredients()) {
            FoodItem food = foodDatabase.get(ingredient.getFoodID());
            if (food != null) {
                double quantity = ingredient.getQuantity() / 100.0; // Convert to 100g basis
                totalCalories += food.getCalories() * quantity;
                
                // Get nutrients from the nutrients map
                Map<String, Double> foodNutrients = food.getNutrients();
                totalProtein += foodNutrients.getOrDefault("Protein", 0.0) * quantity;
                totalCarbs += foodNutrients.getOrDefault("Carbs", 0.0) * quantity;
                totalFat += foodNutrients.getOrDefault("Fat", 0.0) * quantity;
                totalFiber += foodNutrients.getOrDefault("Fiber", 0.0) * quantity;
            }
        }
        
        nutrients.put("Calories", totalCalories);
        nutrients.put("Protein", totalProtein);
        nutrients.put("Carbs", totalCarbs);
        nutrients.put("Fat", totalFat);
        nutrients.put("Fiber", totalFiber);
        
        return nutrients;
    }
    
    private void initializeMockFoodDatabase() {
        // Mock food items for demonstration - using correct FoodItem constructor
        Map<String, Double> whiteRiceNutrients = Map.of(
            "Protein", 2.7,
            "Carbs", 28.0,
            "Fat", 0.3,
            "Fiber", 0.4
        );
        
        Map<String, Double> brownRiceNutrients = Map.of(
            "Protein", 2.6,
            "Carbs", 23.0,
            "Fat", 0.9,
            "Fiber", 1.8
        );
        
        Map<String, Double> regularYogurtNutrients = Map.of(
            "Protein", 10.0,
            "Carbs", 3.6,
            "Fat", 0.4,
            "Fiber", 0.0
        );
        
        Map<String, Double> greekYogurtNutrients = Map.of(
            "Protein", 10.0,
            "Carbs", 3.6,
            "Fat", 0.4,
            "Fiber", 0.0
        );
        
        foodDatabase.put(1, new FoodItem(1, "White Rice", 130, whiteRiceNutrients, "Grains"));
        foodDatabase.put(2, new FoodItem(2, "Brown Rice", 111, brownRiceNutrients, "Grains"));
        foodDatabase.put(3, new FoodItem(3, "Regular Yogurt", 59, regularYogurtNutrients, "Dairy"));
        foodDatabase.put(4, new FoodItem(4, "Greek Yogurt", 59, greekYogurtNutrients, "Dairy"));
    }
    
    // Inner classes for data structures
    public static class NutrientChange {
        private double originalValue;
        private double newValue;
        private double absoluteChange;
        private double percentChange;
        
        public NutrientChange(double original, double newValue, double absoluteChange, double percentChange) {
            this.originalValue = original;
            this.newValue = newValue;
            this.absoluteChange = absoluteChange;
            this.percentChange = percentChange;
        }
        
        // Getters
        public double getOriginalValue() { return originalValue; }
        public double getNewValue() { return newValue; }
        public double getAbsoluteChange() { return absoluteChange; }
        public double getPercentChange() { return percentChange; }
        
        public boolean isImproved() {
            return absoluteChange > 0;
        }
    }
    
    public static class NutritionComparison {
        private Map<String, NutrientChange> changes;
        
        public NutritionComparison(Map<String, NutrientChange> changes) {
            this.changes = changes;
        }
        
        public Map<String, NutrientChange> getChanges() {
            return changes;
        }
        
        public List<String> getImprovedNutrients() {
            return changes.entrySet().stream()
                .filter(entry -> entry.getValue().isImproved())
                .map(Map.Entry::getKey)
                .toList();
        }
    }
} 