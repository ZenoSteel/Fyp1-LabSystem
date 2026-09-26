package com.example.fyp1;

import java.io.Serializable;

/**
 * Data model representing a Lecturer in the directory.
 * Implements Serializable to be passed between activities via Intents.
 */
public class Lecturer implements Serializable {
    private String name;
    private String office;
    private String phone;
    private String labCoordinator;
    private int profileImageResId;

    public Lecturer(String name, String office, String phone, String labCoordinator, int profileImageResId) {
        this.name = name;
        this.office = office;
        this.phone = phone;
        this.labCoordinator = labCoordinator;
        this.profileImageResId = profileImageResId;
    }

    // Getters
    public String getName() { return name; }
    public String getOffice() { return office; }
    public String getPhone() { return phone; }
    public String getLabCoordinator() { return labCoordinator; }
    public int getProfileImageResId() { return profileImageResId; }
}