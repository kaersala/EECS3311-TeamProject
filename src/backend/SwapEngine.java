package backend;

import model.*;
import model.meal.IngredientEntry;
import strategy.*;
import service.NutrientCalculator;
import service.SwapReasonGenerator;
import model.IntensityLevel;

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
        Map<String, Double> currentNutrients = NutrientCalculator.calculateMealNutrients(currentMeal, foodDatabase);
        
        // Generate suggestions for each goal, but limit total number
        for (Goal goal : goals) {
            if (suggestions.size() >= MAX_SWAPS_PER_MEAL) break;
            
            double intensityFactor = getIntensityFactor(goal.getIntensity());
            RecommendationStrategy strategy = selectStrategy(goal);

            if (strategy == null) continue;

            // Find best replacement for current goal
            GoalContext goalContext = new GoalContext(goal);
            SwapSuggestion bestSuggestion = findBestSwapForGoal(goalContext, currentMeal, foodDatabase, currentNutrients, strategy);
            
            if (bestSuggestion != null) {
                suggestions.add(bestSuggestion);
            }
        }

        return suggestions;
    }

    private SwapSuggestion findBestSwapForGoal(GoalContext goalContext, List<IngredientEntry> currentMeal, 
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
                    .filter(item -> isBetterForGoal(item, original, goalContext))
                    .toList();

            for (FoodItem candidate : candidates) {
                // Check nutrient balance after replacement
                if (isNutrientBalanced(original, candidate, currentNutrients, goalContext)) {
                    // Calculate overall score for replacement
                    double score = calculateSwapScore(original, candidate, goalContext, currentNutrients);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        String reason = SwapReasonGenerator.generateReason(original, candidate, goalContext);
                        bestSuggestion = new SwapSuggestion(entry, new IngredientEntry(candidate.getFoodID(), entry.getQuantity()), reason);
                    }
                }
            }
        }
        
        return bestSuggestion;
    }



        private boolean isNutrientBalanced(FoodItem original, FoodItem replacement, 
                                      Map<String, Double> currentNutrients, GoalContext goalContext) {
        String targetNutrient = goalContext.getTargetNutrient();
        
        for (Map.Entry<String, Double> nutrient : currentNutrients.entrySet()) {
            String nutrientName = nutrient.getKey().toLowerCase();
            
            // Skip target nutrient
            if (nutrientName.equals(targetNutrient)) continue;
            
            double changePercentage = calculateNutrientChange(original, replacement, nutrientName, nutrient.getValue());
            
            if (changePercentage > NUTRIENT_TOLERANCE) {
                return false; // Change too large, unbalanced
            }
        }
        
        return true;
    }

    private double calculateNutrientChange(FoodItem original, FoodItem replacement, 
                                         String nutrientName, double currentTotal) {
        double originalValue = original.getNutrientValue(nutrientName);
        double replacementValue = replacement.getNutrientValue(nutrientName);
        double difference = replacementValue - originalValue;
        
        return Math.abs(difference) / currentTotal;
    }

    private double calculateSwapScore(FoodItem original, FoodItem replacement, GoalContext goalContext, Map<String, Double> currentNutrients) {
        double baseScore = calculateBaseScore(original, replacement, goalContext);
        double targetBonus = calculateTargetBonus(original, replacement, goalContext, currentNutrients);
        double groupBonus = calculateGroupBonus(original, replacement);
        
        return baseScore + targetBonus + groupBonus;
    }

    private double calculateBaseScore(FoodItem original, FoodItem replacement, GoalContext goalContext) {
        double originalValue = original.getNutrientValue(goalContext.getTargetNutrient());
        double replacementValue = replacement.getNutrientValue(goalContext.getTargetNutrient());
        double difference = replacementValue - originalValue;
        
        return goalContext.getDirection().equals("increase") ? difference : -difference;
    }

    private double calculateTargetBonus(FoodItem original, FoodItem replacement, GoalContext goalContext, Map<String, Double> currentNutrients) {
        double originalValue = original.getNutrientValue(goalContext.getTargetNutrient());
        double replacementValue = replacement.getNutrientValue(goalContext.getTargetNutrient());
        double currentTotal = currentNutrients.getOrDefault(goalContext.getTargetNutrient(), 0.0);
        double newTotal = currentTotal - originalValue + replacementValue;
        
        if (goalContext.getDirection().equals("increase") && newTotal >= currentTotal + goalContext.getTargetAmount()) {
            return 10.0; // Bonus score for reaching target
        } else if (goalContext.getDirection().equals("decrease") && newTotal <= currentTotal - goalContext.getTargetAmount()) {
            return 10.0; // Bonus score for reaching target
        }
        
        return 0.0;
    }

    private double calculateGroupBonus(FoodItem original, FoodItem replacement) {
        return original.getFoodGroup().equals(replacement.getFoodGroup()) ? 2.0 : 0.0;
    }



    private boolean isBetterForGoal(FoodItem candidate, FoodItem original, GoalContext goalContext) {
        double originalValue = original.getNutrientValue(goalContext.getTargetNutrient());
        double candidateValue = candidate.getNutrientValue(goalContext.getTargetNutrient());
        
        if (goalContext.getDirection().equals("increase")) {
            return candidateValue > originalValue;
        } else if (goalContext.getDirection().equals("decrease")) {
            return candidateValue < originalValue;
        }
        
        return false;
    }
    

    


    private RecommendationStrategy selectStrategy(Goal goal) {
        return StrategyFactory.createStrategy(goal.getNutrient());
    }

    private double getIntensityFactor(String intensity) {
        return IntensityLevel.fromString(intensity).getFactor();
    }
}