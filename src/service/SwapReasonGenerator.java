package service;

import model.FoodItem;
import model.GoalContext;

public class SwapReasonGenerator {
    public static String generateReason(FoodItem original, FoodItem replacement, GoalContext goalContext) {
        String targetNutrient = goalContext.getTargetNutrient();
        String direction = goalContext.getDirection();
        
        double originalValue = original.getNutrientValue(targetNutrient);
        double replacementValue = replacement.getNutrientValue(targetNutrient);
        
        if (direction.equals("increase")) {
            if (replacementValue > originalValue) {
                return String.format("Replace %s with %s to increase %s intake (%.1f → %.1f)",
                    original.getName(), replacement.getName(), targetNutrient, originalValue, replacementValue);
            }
        } else if (direction.equals("decrease")) {
            if (replacementValue < originalValue) {
                return String.format("Replace %s with %s to decrease %s intake (%.1f → %.1f)",
                    original.getName(), replacement.getName(), targetNutrient, originalValue, replacementValue);
            }
        }
        
        return String.format("Replace %s with %s for better %s balance",
            original.getName(), replacement.getName(), targetNutrient);
    }
} 