package model.user;

public class Settings {
    private String units; // "metric" or "imperial"

    public Settings(String units) {
        this.units = units;
    }

    public String getUnits() { return units; }
    public void setUnits(String units) { this.units = units; }
}

