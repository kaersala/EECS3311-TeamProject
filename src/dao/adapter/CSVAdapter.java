package dao.adapter;

import model.FoodItem;
import model.user.UserProfile;
import model.meal.Meal;
import model.meal.IngredientEntry;

import java.io.*;
import java.util.*;

public class CSVAdapter {

    public static void saveFoodItemsToCSV(List<FoodItem> foodItems, String filePath) {
        try (PrintWriter writer = new PrintWriter(new File(filePath))) {
            writer.println("ID,Name,Calories,Group,Nutrients");
            for (FoodItem item : foodItems) {
                writer.print(item.getFoodID() + ",");
                writer.print(item.getName() + ",");
                writer.print(item.getCalories() + ",");
                writer.print(item.getFoodGroup() + ",");

                Map<String, Double> nutrients = item.getNutrients();
                List<String> nutrientPairs = new ArrayList<>();
                for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
                    nutrientPairs.add(entry.getKey() + ":" + entry.getValue());
                }
                writer.println(String.join("|", nutrientPairs));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FoodItem> loadFoodItemsFromCSV(String filePath) {
        List<FoodItem> foodItems = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 5);
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                double calories = Double.parseDouble(parts[2]);
                String group = parts[3];
                Map<String, Double> nutrients = new HashMap<>();

                String[] nutrientPairs = parts[4].split("\\|");
                for (String pair : nutrientPairs) {
                    String[] kv = pair.split(":");
                    nutrients.put(kv[0], Double.parseDouble(kv[1]));
                }

                foodItems.add(new FoodItem(id, name, calories, nutrients, group));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return foodItems;
    }

    public static void saveUserProfilesToCSV(List<UserProfile> profiles, String filePath) {
        try (PrintWriter writer = new PrintWriter(new File(filePath))) {
            writer.println("ID,Name,DOB,Sex,Height,Weight,Units");
            for (UserProfile profile : profiles) {
                writer.printf("%d,%s,%s,%s,%.2f,%.2f,%s\n",
                        profile.getUserID(),
                        profile.getName(),
                        profile.getDob(),
                        profile.getSex(),
                        profile.getHeight(),
                        profile.getWeight(),
                        profile.getSettings().getUnits());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMealsToCSV(List<Meal> meals, String filePath) {
        try (PrintWriter writer = new PrintWriter(new File(filePath))) {
            writer.println("MealID,UserID,Date,Type,FoodID,Quantity");
            for (Meal meal : meals) {
                for (IngredientEntry entry : meal.getIngredients()) {
                    writer.printf("%d,%d,%s,%s,%d,%.2f\n",
                            meal.getMealID(),
                            meal.getUserID(),
                            meal.getDate(),
                            meal.getType(),
                            entry.getFoodID(),
                            entry.getQuantity());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
