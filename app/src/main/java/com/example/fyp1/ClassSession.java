package com.example.fyp1;

import java.io.Serializable;

/**
 * Data model for an official academic class session.
 */
public class ClassSession implements Serializable {
    private String classCode;
    private String subjectName;
    private String lecturerName;
    private int semester;
    private String day;
    private String timeSlot;
    private String labLocation;

    public ClassSession(String classCode, String subjectName, String lecturerName, int semester, String day, String timeSlot, String labLocation) {
        this.classCode = classCode != null ? classCode : "";
        this.subjectName = subjectName != null ? subjectName : "";
        this.lecturerName = lecturerName != null ? lecturerName : "";
        this.semester = semester;
        this.day = day != null ? day : "Monday";
        this.timeSlot = timeSlot != null ? timeSlot : "";
        this.labLocation = labLocation != null ? labLocation : "";
    }

    // Legacy constructor for backward compatibility
    public ClassSession(String subjectName, int semester, String day, String timeSlot, String labLocation) {
        this("DFC501", subjectName, "Faculty Lecturer", semester, day, timeSlot, labLocation);
    }

    public String getClassCode() { return classCode; }
    public String getSubjectName() { return subjectName; }
    public String getLecturerName() { return lecturerName; }
    public int getSemester() { return semester; }
    public String getDay() { return day; }
    public String getTimeSlot() { return timeSlot; }
    public String getLabLocation() { return labLocation; }

    @Override
    public String toString() {
        String codeStr = classCode.isEmpty() ? "" : classCode + " - ";
        return codeStr + subjectName + " (" + day + " " + timeSlot + ")";
    }
}
