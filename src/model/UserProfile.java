package model;

import java.time.LocalDate;

public class UserProfile {
    private int userID;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String sex;
    private double height; // in cm or inches
    private double weight; // in kg or pounds
    private Settings settings;

    public UserProfile() {
        this.settings = new Settings(); // default settings
    }

    // Getters
    public int getUserID() { return userID; }

    public String getFirstName() { return firstName;}
    public String getLastNameName() { return lastName; }
    public LocalDate getDob() { return dob; }
    public String getSex() { return sex; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public Settings getSettings() { return settings; }

    // Setters
    public void setUserID(int userID) { this.userID = userID; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String name) { this.lastName = lastName; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public void setSex(String sex) { this.sex = sex; }
    public void setHeight(double height) { this.height = height; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setSettings(Settings settings) { this.settings = settings; }

    @Override
    public String toString() {
        return firstName + lastName + " (" + userID + ")";
    }
}
