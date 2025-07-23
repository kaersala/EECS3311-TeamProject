package model.user;

import java.time.LocalDate;

public class UserProfile {
    private int userID;
    private String name;
    private LocalDate dob;
    private String sex;
    private double height; // in cm or inches
    private double weight; // in kg or pounds
    private Settings settings;

    // Default constructor
    public UserProfile() {
        this.settings = new Settings("Metric");
    }

    public UserProfile(String name, String sex, LocalDate dob, double height, double weight) {
        this.name = name;
        this.sex = sex;
        this.dob = dob;
        this.height = height;
        this.weight = weight;
        this.settings = new Settings("Metric");
    }

    // Getters
    public int getUserID() { return userID; }
    public String getName() { return name; }
    public LocalDate getDob() { return dob; }
    public String getSex() { return sex; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public Settings getSettings() { return settings; }

    // Setters
    public void setUserID(int userID) { this.userID = userID; }
    public void setName(String name) { this.name = name; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public void setSex(String sex) { this.sex = sex; }
    public void setHeight(double height) { this.height = height; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setSettings(Settings settings) { this.settings = settings; }

    // Helper method for string date
    public void setDob(String dobString) {
        if (dobString != null && !dobString.isEmpty()) {
            this.dob = LocalDate.parse(dobString);
        }
    }


    @Override
    public String toString() {
        return name + " (" + userID + ")";
    }
}