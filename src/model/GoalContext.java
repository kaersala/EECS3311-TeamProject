package model;

public class GoalContext {
    private final String targetNutrient;
    private final String direction;
    private final double targetAmount;
    
    public GoalContext(Goal goal) {
        this.targetNutrient = goal.getNutrient().toLowerCase();
        this.direction = goal.getDirection().toLowerCase();
        this.targetAmount = goal.getAmount();
    }
    
    public String getTargetNutrient() { 
        return targetNutrient; 
    }
    
    public String getDirection() { 
        return direction; 
    }
    
    public double getTargetAmount() { 
        return targetAmount; 
    }
} 