package model;

/**
 * Represents a nutrient and its amount per unit (e.g., per 100g).
 */
public class Nutrient {
    private int nutrientId;
    private String name;
    private String unit; // e.g., "g", "mg", "kcal"
    private double amount; // value per serving or per 100g

    public Nutrient(int nutrientId, String name, String unit, double amount) {
        this.nutrientId = nutrientId;
        this.name = name;
        this.unit = unit;
        this.amount = amount;
    }

    // Getters
    public int getNutrientId() {
        return nutrientId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public double getAmount() {
        return amount;
    }

    // Setters
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNutrientId(int nutrientId) {
        this.nutrientId = nutrientId;
    }

    @Override
    public String toString() {
        return name + ": " + amount + " " + unit;
    }
}
