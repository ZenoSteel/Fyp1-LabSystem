package com.example.fyp1;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages student schedules and master lab timetables with persistent storage.
 */
public class ScheduleManager {
    private static final String PREF_NAME = "MasterScheduleData";
    private static final String KEY_MASTER_TIMETABLE = "master_timetable_v5";
    private static final String KEY_MY_SCHEDULE = "my_schedule";

    public static List<ClassSession> getMasterTimetable(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_MASTER_TIMETABLE, null);
        List<ClassSession> master = new ArrayList<>();

        if (json == null) {
            saveMasterTimetable(context, master);
            return master;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                master.add(new ClassSession(
                    obj.optString("classCode", ""),
                    obj.getString("subjectName"),
                    obj.optString("className", "Class " + obj.optInt("semester", 1) + "A"),
                    obj.optString("lecturerName", "Faculty Staff"),
                    obj.getInt("semester"),
                    obj.getString("day"),
                    obj.getString("timeSlot"),
                    obj.getString("labLocation")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return master;
    }

    public static List<ClassSession> getMasterTimetable() {
        return new ArrayList<>();
    }

    public static void addTimetableSession(Context context, ClassSession session) {
        List<ClassSession> currentList = getMasterTimetable(context);
        currentList.add(session);
        saveMasterTimetable(context, currentList);
    }

    public static void removeTimetableSession(Context context, ClassSession targetSession) {
        if (targetSession == null) return;
        List<ClassSession> currentList = getMasterTimetable(context);
        List<ClassSession> updatedList = new ArrayList<>();

        for (ClassSession s : currentList) {
            if (s.getLabLocation().equalsIgnoreCase(targetSession.getLabLocation()) &&
                s.getDay().equalsIgnoreCase(targetSession.getDay()) &&
                s.getTimeSlot().equalsIgnoreCase(targetSession.getTimeSlot()) &&
                s.getSubjectName().equalsIgnoreCase(targetSession.getSubjectName())) {
                continue; // Skip target session to delete it
            }
            updatedList.add(s);
        }

        saveMasterTimetable(context, updatedList);
    }

    public static void saveMasterTimetable(Context context, List<ClassSession> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        try {
            JSONArray array = new JSONArray();
            for (ClassSession s : list) {
                JSONObject obj = new JSONObject();
                obj.put("classCode", s.getClassCode());
                obj.put("subjectName", s.getSubjectName());
                obj.put("className", s.getClassName());
                obj.put("lecturerName", s.getLecturerName());
                obj.put("semester", s.getSemester());
                obj.put("day", s.getDay());
                obj.put("timeSlot", s.getTimeSlot());
                obj.put("labLocation", s.getLabLocation());
                array.put(obj);
            }
            prefs.edit().putString(KEY_MASTER_TIMETABLE, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<ClassSession> getStudentSchedule(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_MY_SCHEDULE, null);
        List<ClassSession> schedule = new ArrayList<>();

        if (json == null) {
            return schedule;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                schedule.add(new ClassSession(
                    obj.optString("classCode", ""),
                    obj.getString("subjectName"),
                    obj.optString("className", "Class " + obj.optInt("semester", 1) + "A"),
                    obj.optString("lecturerName", "Faculty Staff"),
                    obj.getInt("semester"),
                    obj.getString("day"),
                    obj.getString("timeSlot"),
                    obj.getString("labLocation")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedule;
    }

    public static void addStudentClass(Context context, ClassSession session) {
        List<ClassSession> currentList = getStudentSchedule(context);
        currentList.add(session);
        saveStudentSchedule(context, currentList);
    }

    public static void removeStudentClass(Context context, ClassSession targetSession) {
        if (targetSession == null) return;
        List<ClassSession> currentList = getStudentSchedule(context);
        List<ClassSession> updatedList = new ArrayList<>();

        for (ClassSession s : currentList) {
            if (s.getDay().equalsIgnoreCase(targetSession.getDay()) &&
                s.getTimeSlot().equalsIgnoreCase(targetSession.getTimeSlot()) &&
                s.getSubjectName().equalsIgnoreCase(targetSession.getSubjectName())) {
                continue;
            }
            updatedList.add(s);
        }

        saveStudentSchedule(context, updatedList);
    }

    public static void saveStudentSchedule(Context context, List<ClassSession> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        try {
            JSONArray array = new JSONArray();
            for (ClassSession s : list) {
                JSONObject obj = new JSONObject();
                obj.put("classCode", s.getClassCode());
                obj.put("subjectName", s.getSubjectName());
                obj.put("className", s.getClassName());
                obj.put("lecturerName", s.getLecturerName());
                obj.put("semester", s.getSemester());
                obj.put("day", s.getDay());
                obj.put("timeSlot", s.getTimeSlot());
                obj.put("labLocation", s.getLabLocation());
                array.put(obj);
            }
            prefs.edit().putString(KEY_MY_SCHEDULE, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
