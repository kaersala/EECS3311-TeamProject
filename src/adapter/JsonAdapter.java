package adapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.FoodItem;
import model.meal.Meal;
import model.user.UserProfile;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonAdapter {
    private final Gson gson = new Gson();

    public List<FoodItem> loadAllFoodItems(String filePath) {
        try (Reader reader = new FileReader("data/json/" + filePath)) {
            Type listType = new TypeToken<List<FoodItem>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            System.err.println("Failed to load food items: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> loadAllJsonStrings(String type) {
        try (Reader reader = new FileReader("data/json/" + type + ".json")) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            System.err.println("Failed to load " + type + " JSON strings: " + e.getMessage());
            return new ArrayList<>();
        }
    }



    public void saveUserProfiles(List<UserProfile> profiles, String filename) {
        try (FileWriter writer = new FileWriter("data/json/" + filename)) {
            gson.toJson(profiles, writer);
        } catch (Exception e) {
            System.err.println("Failed to save user profiles: " + e.getMessage());
        }
    }

    public Meal deserializeMeal(String json) {
        try {
            return gson.fromJson(json, Meal.class);
        } catch (Exception e) {
            System.err.println("Failed to deserialize meal: " + e.getMessage());
            return null;
        }
    }

    public UserProfile deserializeUserProfile(String json) {
        try {
            return gson.fromJson(json, UserProfile.class);
        } catch (Exception e) {
            System.err.println("Failed to deserialize user profile: " + e.getMessage());
            return null;
        }
    }

    public List<Meal> loadMeals() {
        try (Reader reader = new FileReader("data/json/meal.json")) {
            Type listType = new TypeToken<List<Meal>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            System.err.println("Failed to load meals: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public UserProfile loadUserProfile() {
        try (Reader reader = new FileReader("data/json/userprofile.json")) {
            return gson.fromJson(reader, UserProfile.class);
        } catch (Exception e) {
            System.err.println("Failed to load user profile: " + e.getMessage());
            return null;
        }
    }

    public void saveUserProfile(UserProfile profile) {
        try (FileWriter writer = new FileWriter("data/json/userprofile.json")) {
            gson.toJson(profile, writer);
        } catch (Exception e) {
            System.err.println("Failed to save user profile: " + e.getMessage());
        }
    }
    
    public void saveMeals(List<Meal> meals) {
        saveMeals(meals, "meals.json");
    }

    // New method with filename
    public void saveMeals(List<Meal> meals, String filename) {
        // Simulated save implementation (replace with real file saving logic if needed)
        System.out.println("Saving " + meals.size() + " meals to " + filename);
        // TODO: serialize and write meals to JSON file named `filename`
    }

}
