package model;

import java.util.Map;

public class FoodItem {
    private int foodID;
    private String name;
    private double calories; 
    private Map<String, Double> nutrients; // Optional, can be empty
    private String foodGroup;

    public FoodItem() {
        // Default constructor
    }

    public FoodItem(int foodID, String name, String foodGroup) {
        this.foodID = foodID;
        this.name = name;
        this.foodGroup = foodGroup;
        this.calories = 0.0;
        this.nutrients = Map.of(); // default empty
    }

    // Full constructor 
    public FoodItem(int foodID, String name, double calories, Map<String, Double> nutrients, String foodGroup) {
        this.foodID = foodID;
        this.name = name;
        this.calories = calories;
        this.nutrients = nutrients;
        this.foodGroup = foodGroup;
    }

    // Getters
    public int getFoodID() {
        return foodID;
    }

    public String getName() {
        return name;
    }

    public double getCalories() {
        return calories;
    }

    public Map<String, Double> getNutrients() {
        return nutrients;
    }

    public String getFoodGroup() {
        return foodGroup;
    }

    // Setters
    public void setFoodID(int foodID) {
        this.foodID = foodID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setNutrients(Map<String, Double> nutrients) {
        this.nutrients = nutrients;
    }

    public void setFoodGroup(String foodGroup) {
        this.foodGroup = foodGroup;
    }

    @Override
    public String toString() {
        return name + " (Group: " + foodGroup + ")";
    }
}
