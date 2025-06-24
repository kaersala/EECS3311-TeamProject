package test;

import model.Goal;
import model.SwapSuggestion;
import model.meal.IngredientEntry;
import model.meal.Meal;
import model.meal.MealType;
import service.NutrientChangesCalculator;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class SwapFunctionalityTest {
//TEST#1
	@Test
	public void testManualSwapChangeCalculation() {
	    // Override getNutrients to simulate nutrients
	    Meal original = new Meal(101, 1, LocalDate.now(), MealType.LUNCH, List.of(new IngredientEntry(1001, 1.0))) {
	        @Override
	        public Map<String, Double> getNutrients() {
	            Map<String, Double> nutrients = new HashMap<>();
	            nutrients.put("fiber", 2.0);  // original fiber
	            return nutrients;
	        }
	    };

	    Meal swapped = new Meal(101, 1, LocalDate.now(), MealType.LUNCH, List.of(new IngredientEntry(2002, 1.0))) {
	        @Override
	        public Map<String, Double> getNutrients() {
	            Map<String, Double> nutrients = new HashMap<>();
	            nutrients.put("fiber", 5.0);  // swapped fiber
	            return nutrients;
	        }
	    };

	    NutrientChangesCalculator calculator = new NutrientChangesCalculator();
	    Map<String, Double> changes = calculator.computeDifferences(original, swapped);

	    assertTrue(changes.containsKey("fiber"), "Should contain fiber change");
	    assertEquals(3.0, changes.get("fiber"), 0.01, "Fiber should increase by 3.0 grams");
	}

}
