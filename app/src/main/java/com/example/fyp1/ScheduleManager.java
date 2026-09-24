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
    private static final String KEY_MASTER_TIMETABLE = "master_timetable_v4";
    private static final String KEY_MY_SCHEDULE = "my_schedule";

    public static List<ClassSession> getMasterTimetable(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_MASTER_TIMETABLE, null);
        List<ClassSession> master = new ArrayList<>();

        if (json == null) {
            // Default master dataset with official classes for all labs
            master.add(new ClassSession("DFC5035", "Ethics & Cyberlaw", "Encik Farhan", 5, "Friday", "2:00 PM - 4:00 PM", "DCN"));

            master.add(new ClassSession("DFC50133", "Programming 1", "Encik Nizam", 1, "Monday", "8:00 AM - 11:00 AM", "DNS"));
            master.add(new ClassSession("DFC30223", "Database Systems", "Encik Nizam", 2, "Thursday", "8:00 AM - 10:00 AM", "DNS"));

            master.add(new ClassSession("DFC50133", "Programming 1", "Puan Sarah", 1, "Wednesday", "2:00 PM - 5:00 PM", "CSD"));
            master.add(new ClassSession("DFC5024", "Web Architecture", "Dr. Aminah", 5, "Wednesday", "11:00 AM - 1:00 PM", "CSD"));

            master.add(new ClassSession("DFC5091", "Project 1", "Dr. Zaim", 5, "Monday", "9:00 AM - 12:00 PM", "APL"));
            master.add(new ClassSession("DFC50133", "Programming 1", "Dr. Aminah", 1, "Friday", "9:00 AM - 12:00 PM", "APL"));

            master.add(new ClassSession("DFC40143", "Networking", "Encik Farhan", 3, "Thursday", "2:00 PM - 4:00 PM", "NWL"));
            master.add(new ClassSession("DFC5082", "Network Security", "Encik Nizam", 5, "Thursday", "2:00 PM - 5:00 PM", "NWL"));

            master.add(new ClassSession("DFC40233", "Operating Systems", "Dr. Aminah", 4, "Friday", "9:00 AM - 12:00 PM", "MML"));
            master.add(new ClassSession("DFC30223", "Database Systems", "Puan Sarah", 2, "Wednesday", "9:00 AM - 11:00 AM", "HYP"));
            master.add(new ClassSession("DFC40233", "Operating Systems", "Encik Farhan", 4, "Wednesday", "8:00 AM - 11:00 AM", "PGL 1"));
            master.add(new ClassSession("DFC40233", "Operating Systems", "Dr. Zaim", 4, "Tuesday", "2:00 PM - 5:00 PM", "PGL 2"));
            master.add(new ClassSession("DFC40143", "Networking", "Puan Sarah", 3, "Monday", "2:00 PM - 4:00 PM", "SAD Lab"));

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
        // Fallback
        List<ClassSession> master = new ArrayList<>();
        master.add(new ClassSession("DFC5035", "Ethics & Cyberlaw", "Encik Farhan", 5, "Friday", "2:00 PM - 4:00 PM", "DCN"));
        master.add(new ClassSession("DFC50133", "Programming 1", "Encik Nizam", 1, "Monday", "8:00 AM - 11:00 AM", "DNS"));
        master.add(new ClassSession("DFC40143", "Networking", "Encik Farhan", 3, "Thursday", "2:00 PM - 4:00 PM", "NWL"));
        master.add(new ClassSession("DFC40233", "Operating Systems", "Dr. Aminah", 4, "Friday", "9:00 AM - 12:00 PM", "MML"));
        return master;
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
            schedule.add(new ClassSession("DFC5091", "Project 1", "Dr. Zaim", 5, "Monday", "9:00 AM - 12:00 PM", "APL"));
            schedule.add(new ClassSession("DFC5082", "Security", "Encik Nizam", 5, "Thursday", "2:00 PM - 5:00 PM", "NWL"));
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
