package model;

import model.meal.IngredientEntry;

/**
 * Represents a suggested food item swap to help meet a nutrition goal.
 */
public class SwapSuggestion {
    private IngredientEntry originalItem;
    private IngredientEntry replacementItem;
    private String reason;

    public SwapSuggestion(IngredientEntry originalItem, IngredientEntry replacementItem, String reason) {
        this.originalItem = originalItem;
        this.replacementItem = replacementItem;
        this.reason = reason;
    }

    // Updated Getters
    public IngredientEntry getOriginal() {
        return originalItem;
    }

    public IngredientEntry getReplacement() {
        return replacementItem;
    }

    public String getReason() {
        return reason;
    }

    // Updated Setters
    public void setOriginal(IngredientEntry originalItem) {
        this.originalItem = originalItem;
    }

    public void setReplacement(IngredientEntry replacementItem) {
        this.replacementItem = replacementItem;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Swap " + originalItem + " with " + replacementItem + " (" + reason + ")";
    }
}