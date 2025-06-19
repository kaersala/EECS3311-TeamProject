package model.meal;

public class IngredientEntry {
    private int foodID;
    private double quantity;

    public IngredientEntry(int foodID, double quantity) {
        this.foodID = foodID;
        this.quantity = quantity;
    }

    public int getFoodID() {
        return foodID;
    }

    public double getQuantity() {
        return quantity;
    }
}
