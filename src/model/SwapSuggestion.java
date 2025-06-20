package model;

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

    // Getters
    public IngredientEntry getOriginalItem() {
        return originalItem;
    }

    public IngredientEntry getReplacementItem() {
        return replacementItem;
    }

    public String getReason() {
        return reason;
    }

    // Setters
    public void setOriginalItem(IngredientEntry originalItem) {
        this.originalItem = originalItem;
    }

    public void setReplacementItem(IngredientEntry replacementItem) {
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
