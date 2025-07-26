package model;
public class Goal {
private String nutrient;
private String direction;
private double amount;
private String intensity;
// Default constructor
public Goal() {
}
// Full constructor
public Goal(String nutrient, String direction, double amount, String intensity) {
this.nutrient = nutrient;
this.direction = direction;
this.amount = amount;
this.intensity = intensity;
}
// Getters
public String getNutrient() {
return nutrient;
}
public String getDirection() {
return direction;
}
public double getAmount() {
return amount;
}
public String getIntensity() {
return intensity;
}
// Setters
public void setNutrient(String nutrient) {
this.nutrient = nutrient;
}
public void setDirection(String direction) {
this.direction = direction;
}
public void setAmount(double amount) {
this.amount = amount;
}
public void setIntensity(String intensity) {
this.intensity = intensity;
}
@Override
public String toString() {
return nutrient + " (" + direction + ", " + amount + ", " + intensity + ")";
}
}