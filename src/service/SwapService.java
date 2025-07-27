package service;

import backend.SwapEngine;
import dao.Implementations.MealDAO;
import dao.Implementations.SwapStatusDAO;
import dao.adapter.DatabaseAdapter;
import dao.adapter.MySQLAdapter;
import model.Goal;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealType;
import model.SwapSuggestion;
import model.FoodItem;

import java.time.LocalDate;
import java.util.*;

public class SwapService {
    private SwapEngine swapEngine;
    private MealDAO mealDAO;
    private SwapStatusDAO swapStatusDAO;
    private Map<Integer, FoodItem> foodDatabase;
    
    public SwapService() {
        this.swapEngine = new SwapEngine();
        this.mealDAO = new MealDAO();
        
        // Use the existing database adapter from Main.java
        // This will be set later via setDatabaseAdapter method
        this.swapStatusDAO = null; // Will be initialized when database adapter is set
        
        this.foodDatabase = new HashMap<>(); // In real app, load from database
        initializeMockFoodDatabase();
    }
    
    /**
     * Set the database adapter for swap status operations
     * This should be called after the database connection is established
     */
    public void setDatabaseAdapter(DatabaseAdapter databaseAdapter) {
        System.out.println("=== SwapService.setDatabaseAdapter called ===");
        System.out.println("DatabaseAdapter: " + (databaseAdapter != null ? "NOT NULL" : "NULL"));
        
        if (databaseAdapter != null) {
            this.swapStatusDAO = new SwapStatusDAO(databaseAdapter);
            System.out.println("SwapStatusDAO initialized successfully");
        } else {
            System.out.println("Warning: DatabaseAdapter is null, SwapStatusDAO not initialized");
        }
    }
    
    /**
     * Apply swaps to a specific meal
     * @param mealId The meal ID to modify
     * @param suggestions List of swap suggestions to apply
     * @param userId The user ID
     * @return The modified meal, or null if failed
     */
    public Meal applySwapsToMeal(int mealId, List<SwapSuggestion> suggestions, int userId) {
        try {
            // Get original meal
            Meal originalMeal = getMealById(mealId, userId);
            if (originalMeal == null) {
                throw new IllegalArgumentException("Meal not found: " + mealId);
            }
            
            // Store original meal data before modification
            System.out.println("DEBUG: Storing original meal data for meal " + mealId + " before swap");
            storeOriginalMealData(originalMeal);
            
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
                Meal modifiedMeal = applySwapsToMeal(meal.getMealID(), suggestions, userId);
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
    
    /**
     * Store rollback data for a specific date with meal details
     * @param date The date for which to store rollback data
     * @param originalMeals The original meals before swap
     */
    public void storeRollbackData(String date, List<Meal> originalMeals) {
        try {
            if (swapStatusDAO == null) {
                System.err.println("Warning: SwapStatusDAO not initialized, cannot store rollback data");
                return;
            }
            
            System.out.println("DEBUG: Storing rollback data for " + originalMeals.size() + " meals on date " + date);
            
            StringBuilder rollbackData = new StringBuilder();
            rollbackData.append("ORIGINAL_MEALS:");
            
            for (Meal meal : originalMeals) {
                System.out.println("DEBUG: Adding meal " + meal.getType() + " with " + meal.getIngredients().size() + " ingredients");
                rollbackData.append("\nMEAL:").append(meal.getType().name()).append(":");
                for (IngredientEntry ingredient : meal.getIngredients()) {
                    rollbackData.append("\n  FOOD:").append(ingredient.getFoodID())
                               .append(":").append(ingredient.getQuantity());
                    System.out.println("DEBUG: Added ingredient FoodID:" + ingredient.getFoodID() + " Quantity:" + ingredient.getQuantity());
                }
            }
            
            String data = rollbackData.toString();
            System.out.println("Storing detailed rollback data for date " + date);
            System.out.println("DEBUG: Complete rollback data: " + data);
            
            // Store in database for each meal
            LocalDate localDate = LocalDate.parse(date);
            for (Meal meal : originalMeals) {
                System.out.println("DEBUG: Storing rollback data for meal " + meal.getType() + " (ID: " + meal.getMealID() + ")");
                swapStatusDAO.markMealAsSwapped(meal.getUserID(), meal.getMealID(), localDate, data);
            }
            
        } catch (Exception e) {
            System.err.println("Error storing rollback data: " + e.getMessage());
        }
    }
    
    /**
     * Get rollback data for a specific date
     * @param date The date for which to get rollback data
     * @return The rollback data, or null if not found
     */
    public String getRollbackData(String date) {
        try {
            if (swapStatusDAO == null) {
                System.err.println("Warning: SwapStatusDAO not initialized, cannot get rollback data");
                return null;
            }
            
            LocalDate localDate = LocalDate.parse(date);
            
            // Try to find swapped meals for different user IDs (since we don't know the exact user ID)
            for (int userId = 1; userId <= 10; userId++) { // Try user IDs 1-10
                List<Integer> swappedMealIds = swapStatusDAO.getSwappedMealIds(userId, localDate);
                if (!swappedMealIds.isEmpty()) {
                    System.out.println("Found swapped meals for user " + userId + " on date " + date);
                    // Get the rollback data from the first swapped meal (all meals have the same complete data)
                    String rollbackData = swapStatusDAO.getOriginalMealData(userId, swappedMealIds.get(0), localDate);
                    if (rollbackData != null && !rollbackData.isEmpty()) {
                        System.out.println("Retrieved rollback data: " + rollbackData.substring(0, Math.min(100, rollbackData.length())) + "...");
                    }
                    return rollbackData;
                }
            }
            
            System.out.println("No swapped meals found for date " + date + " in any user");
            
        } catch (Exception e) {
            System.err.println("Error getting rollback data: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Restore original meal from rollback data
     * @param mealId The meal ID to restore
     * @param rollbackData The rollback data containing original meal information
     * @param userId The user ID
     */
    public void restoreOriginalMeal(int mealId, String rollbackData, int userId) {
        try {
            if (swapStatusDAO == null) {
                System.err.println("Warning: SwapStatusDAO not initialized, cannot restore meal");
                return;
            }
            
            // Parse rollback data to get original meal information
            if (rollbackData != null && rollbackData.startsWith("ORIGINAL_MEALS:")) {
                // Parse the detailed rollback data
                String[] lines = rollbackData.split("\n");
                
                // Find the meal type for this specific meal ID
                // We need to get the original date first
                LocalDate originalDate = null;
                try {
                    // Get the original date from the meal
                    Meal currentMeal = getMealById(mealId, userId);
                    if (currentMeal != null) {
                        originalDate = currentMeal.getDate();
                    } else {
                        System.err.println("Could not find meal with ID: " + mealId);
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Error getting original date: " + e.getMessage());
                    return;
                }
                
                // Parse rollback data to find the correct meal based on meal type
                String targetMealType = null;
                List<IngredientEntry> originalIngredients = new ArrayList<>();
                
                // First, get the current meal type to know which meal to restore
                Meal currentMeal = getMealById(mealId, userId);
                if (currentMeal != null) {
                    targetMealType = currentMeal.getType().name();
                    System.out.println("Looking for meal type: " + targetMealType + " for meal ID: " + mealId);
                } else {
                    System.err.println("Could not determine meal type for meal ID: " + mealId);
                    return;
                }
                
                // Parse rollback data to find the matching meal type
                boolean foundTargetMeal = false;
                String currentMealType = "";
                List<IngredientEntry> currentIngredients = new ArrayList<>();
                
                for (String line : lines) {
                    if (line.startsWith("MEAL:")) {
                        // If we found a new meal and we were processing the target meal, break
                        if (foundTargetMeal && currentMealType.equals(targetMealType)) {
                            break;
                        }
                        
                        // Remove "MEAL:" prefix and any trailing colon
                        String mealTypeStr = line.substring(5);
                        if (mealTypeStr.endsWith(":")) {
                            mealTypeStr = mealTypeStr.substring(0, mealTypeStr.length() - 1);
                        }
                        currentMealType = mealTypeStr;
                        currentIngredients.clear(); // Clear previous meal's ingredients
                        System.out.println("Parsed meal type: " + currentMealType);
                        
                        // Check if this is the meal we're looking for
                        if (currentMealType.equals(targetMealType)) {
                            foundTargetMeal = true;
                        }
                    } else if (line.startsWith("  FOOD:") && foundTargetMeal && currentMealType.equals(targetMealType)) {
                        String[] parts = line.substring(7).split(":"); // Remove "  FOOD:" prefix
                        if (parts.length >= 2) {
                            int foodId = Integer.parseInt(parts[0]);
                            double quantity = Double.parseDouble(parts[1]);
                            currentIngredients.add(new IngredientEntry(foodId, quantity));
                        }
                    }
                }
                
                // Use the ingredients we found for the target meal
                if (foundTargetMeal && !currentIngredients.isEmpty()) {
                    originalIngredients = new ArrayList<>(currentIngredients);
                    System.out.println("Found target meal: " + targetMealType + " with " + originalIngredients.size() + " ingredients");
                } else {
                    System.err.println("Could not find target meal type: " + targetMealType + " in rollback data");
                    return;
                }
                
                // Create original meal with parsed data and original date
                Meal originalMeal = new Meal(mealId, userId, originalDate, 
                    MealType.valueOf(targetMealType), originalIngredients);
                
                System.out.println("Restoring meal " + mealId + " to original state:");
                System.out.println("  Date: " + originalDate);
                System.out.println("  Type: " + targetMealType);
                System.out.println("  Ingredients: " + originalIngredients.size());
                
                // Update the meal in database
                mealDAO.updateMeal(originalMeal);
                
                // Mark meal as restored in swap status table
                swapStatusDAO.markMealAsRestored(userId, mealId, originalDate);
                
                System.out.println("Successfully restored original meal " + mealId + " with " + originalIngredients.size() + " ingredients");
            } else {
                System.err.println("Invalid rollback data format");
            }
        } catch (Exception e) {
            System.err.println("Error restoring original meal: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if meals for a specific date have been swapped
     * @param userId User ID
     * @param date Date to check
     * @return true if any meals for this date have been swapped
     */
    public boolean hasMealsBeenSwapped(int userId, LocalDate date) {
        System.out.println("=== hasMealsBeenSwapped called ===");
        System.out.println("userId: " + userId + ", date: " + date);
        System.out.println("swapStatusDAO: " + (swapStatusDAO != null ? "NOT NULL" : "NULL"));
        
        try {
            if (swapStatusDAO == null) {
                System.err.println("Warning: SwapStatusDAO not initialized, returning false");
                return false;
            }
            boolean result = swapStatusDAO.hasMealsBeenSwapped(userId, date);
            System.out.println("Database query result: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Error checking if meals have been swapped: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a specific meal has been swapped
     * @param mealId Meal ID to check
     * @param userId User ID
     * @return true if the meal has been swapped
     */
    public boolean hasMealBeenSwapped(int mealId, int userId) {
        try {
            Meal meal = getMealById(mealId, userId);
            if (meal == null) {
                return false;
            }
            
            // Check if rollback data exists for this meal's date
            return hasMealsBeenSwapped(userId, meal.getDate());
        } catch (Exception e) {
            System.err.println("Error checking meal swap status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark a meal as restored (not swapped)
     * @param mealId The meal ID to mark as restored
     * @param userId The user ID
     */
    public void markMealAsRestored(int mealId, int userId) {
        try {
            if (swapStatusDAO == null) {
                System.err.println("Warning: SwapStatusDAO not initialized, cannot mark meal as restored");
                return;
            }
            
            // Get the meal to find its date
            Meal meal = getMealById(mealId, userId);
            if (meal != null) {
                swapStatusDAO.markMealAsRestored(userId, mealId, meal.getDate());
                System.out.println("DEBUG: Marked meal " + mealId + " as restored");
            } else {
                System.err.println("Could not find meal with ID: " + mealId);
            }
            
        } catch (Exception e) {
            System.err.println("Error marking meal as restored: " + e.getMessage());
        }
    }
    
    // Helper methods
    private Meal getMealById(int mealId, int userId) {
        try {
            // Get all meals for the user and find the one with matching mealId
            List<Meal> userMeals = mealDAO.getMealsByUserId(userId);
            if (userMeals != null) {
                for (Meal meal : userMeals) {
                    if (meal.getMealID() == mealId) {
                        return meal;
                    }
                }
            }
            System.err.println("Meal not found: " + mealId + " for user: " + userId);
            return null;
        } catch (Exception e) {
            System.err.println("Error getting meal by ID: " + e.getMessage());
            return null;
        }
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
    
    /**
     * Store original meal data for potential rollback
     * @param originalMeal The original meal to store
     */
    public void storeOriginalMealData(Meal originalMeal) {
        try {
            if (swapStatusDAO == null) {
                System.err.println("Warning: SwapStatusDAO not initialized, cannot store original meal data");
                return;
            }
            
            // Check if meal has a valid ID
            if (originalMeal.getMealID() <= 0) {
                System.err.println("Warning: Meal has invalid ID (" + originalMeal.getMealID() + "), cannot store swap status");
                return;
            }
            
            StringBuilder mealData = new StringBuilder();
            mealData.append("ORIGINAL:"); // Simplified prefix
            
            for (IngredientEntry ingredient : originalMeal.getIngredients()) {
                mealData.append(ingredient.getFoodID()).append(",").append(ingredient.getQuantity()).append(";");
            }
            
            String data = mealData.toString();
            System.out.println("DEBUG: Storing original meal data for meal " + originalMeal.getMealID() + ": " + data);
            
            boolean success = swapStatusDAO.markMealAsSwapped(originalMeal.getUserID(), originalMeal.getMealID(), originalMeal.getDate(), data);
            if (!success) {
                System.err.println("Warning: Failed to mark meal " + originalMeal.getMealID() + " as swapped");
            }
            
        } catch (Exception e) {
            System.err.println("Error storing original meal data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get original meal data for a specific meal
     * @param mealId The meal ID
     * @param userId The user ID
     * @param date The date
     * @return The original meal data, or null if not found
     */
    public String getOriginalMealData(int mealId, int userId, LocalDate date) {
        try {
            if (swapStatusDAO == null) {
                System.err.println("Warning: SwapStatusDAO not initialized, cannot get original meal data");
                return null;
            }
            
            return swapStatusDAO.getOriginalMealData(userId, mealId, date);
            
        } catch (Exception e) {
            System.err.println("Error getting original meal data: " + e.getMessage());
        }
        return null;
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