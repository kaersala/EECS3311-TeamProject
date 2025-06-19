package model.meal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Meal {
    private String name;
    private Date date;
    private int calories;

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public int getCalories() {
        return calories;
    }
    public Meal(Date date, String name, int calories) {
        this.date = date;
        this.name = name;
        this.calories = calories;
    }
    // Placeholder method to simulate nutrient data
    public Map<String, Double> getNutrients() {
        Map<String, Double> nutrients = new HashMap<>();
        nutrients.put("Protein", 20.0);
        nutrients.put("Carbohydrates", 35.0);
        nutrients.put("Fats", 10.0);
        nutrients.put("Fiber", 5.0);
        return nutrients;
    }
}
