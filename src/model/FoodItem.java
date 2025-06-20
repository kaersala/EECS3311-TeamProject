package model;
import java.util.Map;
public class FoodItem {
private int foodID;
private String name;
private double calories;
private Map<String, Double> nutrients; // e.g., {"Protein": 12.0, "Sodium": 300.0, ...}
private String foodGroup;
public FoodItem() {
// default constructor
}
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
return name + " (" + calories + " kcal)";
}
}
