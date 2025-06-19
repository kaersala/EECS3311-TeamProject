package model.meal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MealBuilder {
    private int userID;
    private LocalDate date;
    private MealType type;
    public List<IngredientEntry> ingredients = new ArrayList<>();

    public MealBuilder setUserId(int userID) {
        this.userID = userID;
        return this;
    }

    public MealBuilder setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public MealBuilder setType(MealType type) {
        this.type = type;
        return this;
    }

    public MealBuilder addIngredient(IngredientEntry entry) {
        ingredients.add(entry);
        return this;
    }

    public Meal build() {
        return new Meal(-1, userID, date, type, ingredients); // mealID can be assigned in DAO
    }
}

