package model;

public enum IntensityLevel {
    LOW(1.0), 
    MODERATE(1.25), 
    HIGH(1.5);
    
    private final double factor;
    
    IntensityLevel(double factor) {
        this.factor = factor;
    }
    
    public double getFactor() {
        return factor;
    }
    
    public static IntensityLevel fromString(String intensity) {
        if (intensity == null) return LOW;
        
        return switch (intensity.toLowerCase()) {
            case "moderate" -> MODERATE;
            case "high" -> HIGH;
            default -> LOW;
        };
    }
} 