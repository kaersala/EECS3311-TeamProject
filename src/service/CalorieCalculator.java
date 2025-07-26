package service;

import model.user.UserProfile;

/**
 * Calculator for BMR (Basal Metabolic Rate), TDEE (Total Daily Energy Expenditure),
 * and recommended calorie intake based on user profile and activity level.
 */
public class CalorieCalculator {

    /**
     * Calculate BMR using Mifflin-St Jeor Equation
     * BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age(years) + 5 (for men)
     * BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age(years) - 161 (for women)
     */
    public static double calculateBMR(UserProfile profile) {
        double weight = profile.getWeight(); // kg
        double height = profile.getHeight(); // cm
        int age = java.time.LocalDate.now().getYear() - profile.getDob().getYear();
        
        if ("Male".equalsIgnoreCase(profile.getSex())) {
            return 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            return 10 * weight + 6.25 * height - 5 * age - 161;
        }
    }

    /**
     * Calculate TDEE based on activity level
     */
    public static double calculateTDEE(UserProfile profile, String activityLevel) {
        double bmr = calculateBMR(profile);
        
        return switch (activityLevel.toLowerCase()) {
            case "sedentary (little or no exercise)" -> bmr * 1.2;
            case "lightly active (light exercise 1-3 days/week)" -> bmr * 1.375;
            case "moderately active (moderate exercise 3-5 days/week)" -> bmr * 1.55;
            case "very active (hard exercise 6-7 days a week)" -> bmr * 1.725;
            case "extra active (very hard exercise, physical job)" -> bmr * 1.9;
            default -> bmr * 1.375; // Default to lightly active
        };
    }

    /**
     * Get recommended calories using Mifflin-St Jeor method
     */
    public static double getRecommendedCaloriesMifflin(UserProfile profile, String activityLevel) {
        return calculateTDEE(profile, activityLevel);
    }

    /**
     * Calculate calories needed for weight loss (deficit)
     */
    public static double getWeightLossCalories(UserProfile profile, String activityLevel, double deficitPercent) {
        double tdee = calculateTDEE(profile, activityLevel);
        return tdee * (1 - deficitPercent / 100.0);
    }

    /**
     * Calculate calories needed for weight gain (surplus)
     */
    public static double getWeightGainCalories(UserProfile profile, String activityLevel, double surplusPercent) {
        double tdee = calculateTDEE(profile, activityLevel);
        return tdee * (1 + surplusPercent / 100.0);
    }
} 