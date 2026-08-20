package com.example.fyp1;

import java.io.Serializable;

/**
 * Data model for an official academic class session.
 */
public class ClassSession implements Serializable {
    private String subjectName;
    private int semester;
    private String day;
    private String timeSlot;
    private String labLocation;

    public ClassSession(String subjectName, int semester, String day, String timeSlot, String labLocation) {
        this.subjectName = subjectName;
        this.semester = semester;
        this.day = day;
        this.timeSlot = timeSlot;
        this.labLocation = labLocation;
    }

    public String getSubjectName() { return subjectName; }
    public int getSemester() { return semester; }
    public String getDay() { return day; }
    public String getTimeSlot() { return timeSlot; }
    public String getLabLocation() { return labLocation; }

    @Override
    public String toString() {
        return subjectName + " (" + day + " " + timeSlot + ")";
    }
}