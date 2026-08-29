package com.example.fyp1;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the student's personal schedule and the master timetable.
 */
public class ScheduleManager {
    private static final String PREF_NAME = "StudentData";
    private static final String KEY_MY_SCHEDULE = "my_schedule";

    public static List<ClassSession> getStudentSchedule(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_MY_SCHEDULE, null);
        List<ClassSession> schedule = new ArrayList<>();

        if (json == null) {
            // Default "Mock" data for demonstration if nothing is saved
            schedule.add(new ClassSession("Project 1", 5, "Monday", "9:00 AM - 12:00 PM", "Lab 3"));
            schedule.add(new ClassSession("Security", 5, "Thursday", "2:00 PM - 5:00 PM", "NWL"));
            return schedule;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                schedule.add(new ClassSession(
                    obj.getString("name"),
                    obj.getInt("sem"),
                    obj.getString("day"),
                    obj.getString("time"),
                    obj.getString("lab")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedule;
    }

    public static List<ClassSession> getMasterTimetable() {
        List<ClassSession> master = new ArrayList<>();
        master.add(new ClassSession("Programming 1", 1, "Monday", "8:00 AM - 11:00 AM", "Lab 1"));
        master.add(new ClassSession("Programming 1", 1, "Wednesday", "2:00 PM - 5:00 PM", "Lab 2"));
        master.add(new ClassSession("Database Systems", 2, "Tuesday", "10:00 AM - 12:00 PM", "DCN"));
        master.add(new ClassSession("Networking", 3, "Thursday", "2:00 PM - 4:00 PM", "NWL"));
        master.add(new ClassSession("Operating Systems", 4, "Friday", "9:00 AM - 12:00 PM", "MML"));
        return master;
    }
}
