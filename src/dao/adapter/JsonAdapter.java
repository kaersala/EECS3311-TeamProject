package adapter;

import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JsonAdapter {
    private final String mealFile = "data/json/meals.json";

    public void saveMeals(List<Meal> meals) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(mealFile))) {
            for (Meal meal : meals) {
                writer.write(serializeMeal(meal));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Meal> loadMeals() {
        List<Meal> meals = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(mealFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Meal meal = deserializeMeal(line);
                if (meal != null) {
                    meals.add(meal);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return meals;
    }

    private String serializeMeal(Meal meal) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"mealID\":").append(meal.getMealID())
          .append(",\"userID\":").append(meal.getUserID())
          .append(",\"name\":\"").append(meal.getName()).append("\"")
          .append(",\"date\":\"").append(meal.getDate()).append("\"")
          .append(",\"type\":\"").append(meal.getType().name()).append("\"")
          .append(",\"calories\":").append(meal.getCalories())
          .append(",\"ingredients\":[");
        List<IngredientEntry> ingredients = meal.getIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            IngredientEntry entry = ingredients.get(i);
            sb.append("{\"foodID\":").append(entry.getFoodID())
              .append(",\"quantity\":").append(entry.getQuantity()).append("}");
            if (i < ingredients.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private Meal deserializeMeal(String json) {
        try {
            json = json.trim().substring(1, json.length() - 1); // 去掉首尾的 {}
            String[] fields = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // 分隔字段（考虑引号内逗号）
    
            int mealID = -1;
            int userID = -1;
            LocalDate date = null;
            MealType type = null;
            List<IngredientEntry> ingredients = new ArrayList<>();
    
            for (String field : fields) {
                String[] keyValue = field.split(":", 2);
                if (keyValue.length != 2) continue;
    
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");
    
                switch (key) {
                    case "mealID":
                        mealID = Integer.parseInt(value);
                        break;
                    case "userID":
                        userID = Integer.parseInt(value);
                        break;
                    case "date":
                        date = LocalDate.parse(value);
                        break;
                    case "type":
                        type = MealType.valueOf(value);
                        break;
                    case "ingredients":
                        // 假设 ingredients 是格式为 [[foodID1, quantity1], [foodID2, quantity2]]
                        value = value.replace("[[", "").replace("]]", "");
                        String[] pairs = value.split("],\\[");
                        for (String pair : pairs) {
                            String[] parts = pair.split(",");
                            if (parts.length == 2) {
                                int foodID = Integer.parseInt(parts[0].trim());
                                double quantity = Double.parseDouble(parts[1].trim());
                                ingredients.add(new IngredientEntry(foodID, quantity));
                            }
                        }
                        break;
                }
            }
    
            return new Meal(mealID, userID, date, type, ingredients);
        } catch (Exception e) {
            System.err.println("Error deserializing meal: " + e.getMessage());
            return null;
        }
    }
  
    public List<String> loadAllJsonStrings(String type) {
        List<String> jsonList = new ArrayList<>();
        File dir = new File(BASE_PATH + type + "s"); // 假设保存路径是 data/meals/
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".json")) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                            StringBuilder json = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                json.append(line);
                            }
                            jsonList.add(json.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return jsonList;
    }

    public void saveMeals(List<Meal> meals, String fileName) {
        File file = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Meal meal : meals) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"mealID\":").append(meal.getMealID()).append(",");
                sb.append("\"userID\":").append(meal.getUserID()).append(",");
                sb.append("\"name\":\"").append(meal.getName()).append("\",");
                sb.append("\"date\":\"").append(meal.getDate()).append("\",");
                sb.append("\"type\":\"").append(meal.getType()).append("\",");
                sb.append("\"calories\":").append(meal.getCalories()).append(",");
                sb.append("\"ingredients\":[");
                List<IngredientEntry> ingredients = meal.getIngredients();
                for (int i = 0; i < ingredients.size(); i++) {
                    IngredientEntry ie = ingredients.get(i);
                    sb.append("{\"foodID\":").append(ie.getFoodID())
                      .append(",\"quantity\":").append(ie.getQuantity()).append("}");
                    if (i < ingredients.size() - 1) sb.append(",");
                }
                sb.append("]");
                sb.append("}");
    
                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
