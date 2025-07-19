package service;

import model.FoodItem;
import model.Goal;
import model.user.UserProfile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates food recommendations based on user's goals or nutrient gaps.
 */
public class RecommendationEngine {

    /**
     * Recommend food items that align with the user's goals.
     *
     * @param userGoals list of nutrient-related goals (e.g., increase fiber, reduce sodium)
     * @param foodItems all available food items
     * @return ranked list of recommended food items
     */
    public List<FoodItem> recommendFoods(List<Goal> userGoals, List<FoodItem> foodItems) {
        Map<FoodItem, Double> scored = new HashMap<>();

        for (FoodItem food : foodItems) {
            double score = 0;

            for (Goal goal : userGoals) {
                String nutrient = goal.getNutrient();
                double value = food.getNutrients().getOrDefault(nutrient, 0.0);
                double intensityFactor = getIntensityFactor(goal.getIntensity());

                if ("increase".equalsIgnoreCase(goal.getDirection())) {
                    score += value * intensityFactor;
                } else if ("decrease".equalsIgnoreCase(goal.getDirection())) {
                    score -= value * intensityFactor;
                }
            }

            scored.put(food, score);
        }

        return scored.entrySet().stream()
                .sorted(Map.Entry.<FoodItem, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Get a simple default intensity factor.
     */
    private double getIntensityFactor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "moderate" -> 1.25;
            case "high" -> 1.5;
            default -> 1.0;
        };
    }

    /**
     * Provide general healthy food suggestions (fallback/default).
     */
    public List<FoodItem> getGeneralSuggestions(List<FoodItem> foodItems) {
        List<String> preferredGroups = List.of("Vegetables", "Fruits", "Whole Grains", "Legumes");

        return foodItems.stream()
                .filter(item -> preferredGroups.contains(item.getFoodGroup()))
                .limit(5)
                .collect(Collectors.toList());
    }
}
