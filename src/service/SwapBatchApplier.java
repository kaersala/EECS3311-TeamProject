package service;

import model.SwapSuggestion;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealBuilder;

import java.util.*;

/**
 * Applies a batch of swap suggestions to an existing meal.
 */
public class SwapBatchApplier {

    /**
     * Applies all swap suggestions to the original meal.
     *
     * @param originalMeal The meal to modify.
     * @param swaps A list of swap suggestions to apply.
     * @return A new Meal instance with all suggested swaps applied.
     */
    public Meal applySwaps(Meal originalMeal, List<SwapSuggestion> swaps) {
        Map<Integer, IngredientEntry> updatedIngredients = new LinkedHashMap<>();

        // Start with original ingredients
        for (IngredientEntry entry : originalMeal.getIngredients()) {
            updatedIngredients.put(entry.getFoodID(), entry);
        }

        // Apply swaps
        for (SwapSuggestion swap : swaps) {
            IngredientEntry original = swap.getOriginal();
            IngredientEntry replacement = swap.getReplacement();
            // Replace the original ingredient with the new one
            updatedIngredients.put(replacement.getFoodID(), replacement);
            updatedIngredients.remove(original.getFoodID());
        }

        // Build a new meal
        MealBuilder builder = new MealBuilder()
                .setMealID(originalMeal.getMealID())
                .setUserId(originalMeal.getUserID())
                .setDate(originalMeal.getDate())
                .setType(originalMeal.getType());

        updatedIngredients.values().forEach(builder::addIngredient);
        return builder.build();
    }
}