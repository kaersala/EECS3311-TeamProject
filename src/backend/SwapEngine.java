package backend;

import model.*;
import model.meal.IngredientEntry;
import strategy.*;

import java.util.*;

public class SwapEngine {

    private static final int MAX_SWAPS_PER_MEAL = 2; // Maximum 2 food items per meal
    private static final double NUTRIENT_TOLERANCE = 0.10; // Allow 10% change in other nutrients

    public List<SwapSuggestion> generateSwaps(List<Goal> goals, List<IngredientEntry> currentMeal, Map<Integer, FoodItem> foodDatabase) {
        List<SwapSuggestion> suggestions = new ArrayList<>();
        
        // Limit number of goals to maximum 2
        if (goals.size() > 2) {
            goals = goals.subList(0, 2);
        }

        // Calculate total nutrients for current meal
        Map<String, Double> currentNutrients = calculateMealNutrients(currentMeal, foodDatabase);
        
        // Generate suggestions for each goal, but limit total number
        for (Goal goal : goals) {
            if (suggestions.size() >= MAX_SWAPS_PER_MEAL) break;
            
            double intensityFactor = getIntensityFactor(goal.getIntensity());
            RecommendationStrategy strategy = selectStrategy(goal);

            if (strategy == null) continue;

            // Find best replacement for current goal
            SwapSuggestion bestSuggestion = findBestSwapForGoal(goal, currentMeal, foodDatabase, currentNutrients, strategy);
            
            if (bestSuggestion != null) {
                suggestions.add(bestSuggestion);
            }
        }

        return suggestions;
    }

    private SwapSuggestion findBestSwapForGoal(Goal goal, List<IngredientEntry> currentMeal, 
                                              Map<Integer, FoodItem> foodDatabase, 
                                              Map<String, Double> currentNutrients,
                                              RecommendationStrategy strategy) {
        
        SwapSuggestion bestSuggestion = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (IngredientEntry entry : currentMeal) {
            FoodItem original = foodDatabase.get(entry.getFoodID());

            if (original == null) {
                System.out.println("Warning: Food item with ID " + entry.getFoodID() + " not found in database");
                continue;
            }

            // Filter candidate replacements (same group, different items, beneficial for goal)
            List<FoodItem> candidates = foodDatabase.values().stream()
                    .filter(item -> item != null && item.getFoodGroup() != null && 
                                   item.getFoodGroup().equals(original.getFoodGroup()))
                    .filter(item -> item.getFoodID() != original.getFoodID())
                    .filter(item -> isBetterForGoal(item, original, goal))
                    .toList();

            for (FoodItem candidate : candidates) {
                // Check nutrient balance after replacement
                if (isNutrientBalanced(original, candidate, currentNutrients, goal)) {
                    // Calculate overall score for replacement
                    double score = calculateSwapScore(original, candidate, goal, currentNutrients);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        String reason = generateReason(original, candidate, goal);
                        bestSuggestion = new SwapSuggestion(entry, new IngredientEntry(candidate.getFoodID(), entry.getQuantity()), reason);
                    }
                }
            }
        }
        
        return bestSuggestion;
    }

    private boolean isNutrientBalanced(FoodItem original, FoodItem replacement, 
                                     Map<String, Double> currentNutrients, Goal goal) {
        // Check if other nutrients are within allowed range after replacement
        String targetNutrient = goal.getNutrient().toLowerCase();
        
        for (Map.Entry<String, Double> nutrient : currentNutrients.entrySet()) {
            String nutrientName = nutrient.getKey().toLowerCase();
            
            // Skip target nutrient
            if (nutrientName.equals(targetNutrient)) continue;
            
            double originalValue = getNutrientValue(original, nutrientName);
            double replacementValue = getNutrientValue(replacement, nutrientName);
            double difference = replacementValue - originalValue;
            
            // Check if change is within allowed range
            double currentTotal = nutrient.getValue();
            double changePercentage = Math.abs(difference) / currentTotal;
            
            if (changePercentage > NUTRIENT_TOLERANCE) {
                return false; // Change too large, unbalanced
            }
        }
        
        return true;
    }

    private double calculateSwapScore(FoodItem original, FoodItem replacement, Goal goal, Map<String, Double> currentNutrients) {
        String targetNutrient = goal.getNutrient().toLowerCase();
        String direction = goal.getDirection().toLowerCase();
        
        double originalValue = getNutrientValue(original, targetNutrient);
        double replacementValue = getNutrientValue(replacement, targetNutrient);
        double difference = replacementValue - originalValue;
        
        // Base score: improvement degree of target nutrient
        double baseScore = direction.equals("increase") ? difference : -difference;
        
        // Check if target amount is reached
        double targetAmount = goal.getAmount();
        double currentTotal = currentNutrients.getOrDefault(targetNutrient, 0.0);
        double newTotal = currentTotal - originalValue + replacementValue;
        
        // If target is reached, give extra score
        if (direction.equals("increase") && newTotal >= currentTotal + targetAmount) {
            baseScore += 10.0; // Bonus score for reaching target
        } else if (direction.equals("decrease") && newTotal <= currentTotal - targetAmount) {
            baseScore += 10.0; // Bonus score for reaching target
        }
        
        // Bonus score for same group replacement
        if (original.getFoodGroup().equals(replacement.getFoodGroup())) {
            baseScore += 2.0;
        }
        
        return baseScore;
    }

    private Map<String, Double> calculateMealNutrients(List<IngredientEntry> meal, Map<Integer, FoodItem> foodDatabase) {
        Map<String, Double> totalNutrients = new HashMap<>();
        
        for (IngredientEntry entry : meal) {
            FoodItem food = foodDatabase.get(entry.getFoodID());
            if (food != null) {
                double quantity = entry.getQuantity();
                
                // Add calories
                totalNutrients.merge("calories", food.getCalories() * quantity / 100.0, Double::sum);
                
                // Add other nutrients
                for (Map.Entry<String, Double> nutrient : food.getNutrients().entrySet()) {
                    totalNutrients.merge(nutrient.getKey().toLowerCase(), 
                                       nutrient.getValue() * quantity / 100.0, Double::sum);
                }
            }
        }
        
        return totalNutrients;
    }

    private boolean isBetterForGoal(FoodItem candidate, FoodItem original, Goal goal) {
        String nutrient = goal.getNutrient().toLowerCase();
        String direction = goal.getDirection().toLowerCase();
        
        double originalValue = getNutrientValue(original, nutrient);
        double candidateValue = getNutrientValue(candidate, nutrient);
        
        if (direction.equals("increase")) {
            return candidateValue > originalValue;
        } else if (direction.equals("decrease")) {
            return candidateValue < originalValue;
        }
        
        return false;
    }
    
    private double getNutrientValue(FoodItem food, String nutrient) {
        Map<String, Double> nutrients = food.getNutrients();
        switch (nutrient) {
            case "calories":
                return food.getCalories();
            case "protein":
                return nutrients.getOrDefault("Protein", 0.0);
            case "carbs":
                return nutrients.getOrDefault("Carbs", 0.0);
            case "fat":
                return nutrients.getOrDefault("Fat", 0.0);
            case "fiber":
                return nutrients.getOrDefault("Fiber", 0.0);
            default:
                return nutrients.getOrDefault(nutrient, 0.0);
        }
    }
    
    private String generateReason(FoodItem original, FoodItem replacement, Goal goal) {
        String nutrient = goal.getNutrient();
        String direction = goal.getDirection();
        
        double originalValue = getNutrientValue(original, nutrient.toLowerCase());
        double replacementValue = getNutrientValue(replacement, nutrient.toLowerCase());
        double difference = replacementValue - originalValue;
        
        String improvement = direction.equals("increase") ? "increases" : "decreases";
        String unit = nutrient.equalsIgnoreCase("calories") ? "calories" : "g";
        
        return String.format("Replace %s with %s to %s %s by %.1f %s", 
                           original.getName(), replacement.getName(), 
                           improvement, nutrient, Math.abs(difference), unit);
    }

    private RecommendationStrategy selectStrategy(Goal goal) {
        return switch (goal.getNutrient().toLowerCase()) {
            case "calories" -> new CalorieSwapStrategy();
            case "fiber" -> new FiberSwapStrategy();
            case "protein" -> new ProteinSwapStrategy();
            case "carbs" -> new CarbsSwapStrategy();
            case "fat" -> new FatSwapStrategy();
            // add more cases like sodium, protein etc.
            default -> null;
        };
    }

    private double getIntensityFactor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "moderate" -> 1.25;
            case "high" -> 1.5;
            default -> 1.0;
        };
    }
}