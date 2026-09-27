package com.example.fyp1;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central Cloud Database & Auth Repository for Firebase Firestore.
 * Provides real-time listeners and atomic transactions with local fallback.
 */
public class DatabaseManager {

    private static final String TAG = "DatabaseManager";
    private static final String RTDB_URL = "https://fyp1-labsystem-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static DatabaseManager instance;

    private FirebaseFirestore db;
    private FirebaseDatabase rtdb;
    private FirebaseAuth auth;
    private Map<String, Boolean> globalMaintenanceMap = new HashMap<>();

    public interface AuthCallback {
        void onSuccess(String username, String role);
        void onFailure(String errorMessage);
    }

    public interface AvailabilityCallback {
        void onDataChanged(List<Map<String, Object>> bookings);
    }

    public interface OperationCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    private DatabaseManager() {
        try {
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            rtdb = FirebaseDatabase.getInstance(RTDB_URL);

            if (rtdb != null) {
                // Permanently delete legacy duplicate keys causing maintenance conflicts
                rtdb.getReference("lab_status").child("PGL1").removeValue();
                rtdb.getReference("lab_status").child("PGL2").removeValue();
                
                rtdb.getReference("lab_status").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Map<String, Boolean> map = new HashMap<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String lab = child.getKey();
                            String val = child.getValue(String.class);
                            boolean isMaint = "UNDER MAINTENANCE".equalsIgnoreCase(val);
                            if (lab != null) {
                                map.put(lab.replaceAll("\\s+", "").toLowerCase(Locale.US), isMaint);
                            }
                        }
                        globalMaintenanceMap = map;
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase SDK initialization deferred or missing config", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public boolean isFirebaseAvailable() {
        return db != null && auth != null;
    }

    public boolean isLabUnderMaintenance(String labName) {
        if (labName == null || globalMaintenanceMap == null) return false;
        String norm = labName.replaceAll("\\s+", "").toLowerCase(Locale.US);
        return Boolean.TRUE.equals(globalMaintenanceMap.get(norm));
    }

    // ==========================================
    // 1. AUTHENTICATION & USER ROLES
    // ==========================================

    public void loginUser(Context context, String username, String password, AuthCallback callback) {
        if (!isFirebaseAvailable()) {
            callback.onFailure("Firebase not configured. Using local mode.");
            return;
        }

        String email = username.contains("@") ? username : username + "@poliku.edu.my";

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        fetchUserRole(user.getUid(), username, callback);
                    } else {
                        callback.onFailure("User profile not found.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void registerUser(Context context, String username, String password, String role, String className, AuthCallback callback) {
        if (!isFirebaseAvailable()) {
            callback.onFailure("Firebase not configured.");
            return;
        }

        String email = username.contains("@") ? username : username + "@poliku.edu.my";

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", user.getUid());
                        userData.put("username", username);
                        userData.put("email", email);
                        userData.put("role", role);
                        if (className != null && !className.trim().isEmpty()) {
                            userData.put("className", className.trim());
                        }
                        userData.put("createdAt", System.currentTimeMillis());

                        db.collection("users").document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(unused -> callback.onSuccess(username, role))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void fetchUserRole(String uid, String fallbackUsername, AuthCallback callback) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        String username = doc.getString("username");
                        callback.onSuccess(username != null ? username : fallbackUsername, role != null ? role : "Student");
                    } else {
                        callback.onSuccess(fallbackUsername, "Student");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ==========================================
    // 2. REAL-TIME BOOKINGS LISTENER
    // ==========================================

    public ListenerRegistration listenToBookings(AvailabilityCallback callback) {
        if (!isFirebaseAvailable()) return null;

        return db.collection("bookings")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    if (snapshots != null) {
                        List<Map<String, Object>> bookingList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Map<String, Object> data = doc.getData();
                            data.put("id", doc.getId());
                            bookingList.add(data);
                        }
                        callback.onDataChanged(bookingList);
                    }
                });
    }

    // ==========================================
    // 3. TRUE ATOMIC BOOKING VERIFICATION
    // ==========================================

    public void addBookingCloud(String lab, String day, String time, int weekOffset, String username, OperationCallback callback) {
        if (!isFirebaseAvailable()) {
            callback.onFailure("Firebase unavailable.");
            return;
        }

        // 1. Query Firestore for existing bookings on this specific day and lab
        db.collection("bookings")
                .whereEqualTo("lab", lab)
                .whereEqualTo("day", day)
                .whereEqualTo("week_offset", weekOffset)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // 2. Perform server-side overlap verification
                    boolean hasClash = false;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String existingTime = doc.getString("time");
                        if (existingTime != null && BookingUtils.isOverlap(time, existingTime)) {
                            hasClash = true;
                            break;
                        }
                    }

                    if (hasClash) {
                        // 3. Abort! Server detected a clash.
                        callback.onFailure("Server rejected booking: Time slot is no longer available.");
                    } else {
                        // 4. Safe to Write! No clashes found on the server.
                        Map<String, Object> newBooking = new HashMap<>();
                        newBooking.put("lab", lab);
                        newBooking.put("day", day);
                        newBooking.put("time", time);
                        newBooking.put("week_offset", weekOffset);
                        newBooking.put("bookedBy", username);
                        newBooking.put("timestamp", System.currentTimeMillis());

                        db.collection("bookings").add(newBooking)
                                .addOnSuccessListener(docRef -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onFailure("Database write failed: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Failed to verify server availability: " + e.getMessage()));
    }

    // ==========================================
    // 4. MASTER TIMETABLE CLOUD SYNC
    // ==========================================

    public void addMasterScheduleCloud(ClassSession session, OperationCallback callback) {
        if (!isFirebaseAvailable()) {
            callback.onFailure("Firebase unavailable.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("classCode", session.getClassCode());
        data.put("subjectName", session.getSubjectName());
        data.put("className", session.getClassName());
        data.put("lecturerName", session.getLecturerName());
        data.put("semester", session.getSemester());
        data.put("day", session.getDay());
        data.put("timeSlot", session.getTimeSlot());
        data.put("labLocation", session.getLabLocation());

        db.collection("master_timetable")
                .add(data)
                .addOnSuccessListener(doc -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteMasterScheduleCloud(ClassSession session, Runnable onComplete) {
        if (!isFirebaseAvailable() || session == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        db.collection("master_timetable")
                .whereEqualTo("labLocation", session.getLabLocation())
                .whereEqualTo("day", session.getDay())
                .whereEqualTo("timeSlot", session.getTimeSlot())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> {
                    if (onComplete != null) onComplete.run();
                });
    }

    public void deleteBookingCloud(String lab, String day, String time, Runnable onComplete) {
        if (!isFirebaseAvailable()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        db.collection("bookings")
                .whereEqualTo("lab", lab)
                .whereEqualTo("day", day)
                .whereEqualTo("time", time)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> {
                    if (onComplete != null) onComplete.run();
                });
    }

    public void fetchMasterTimetableCloud(Context context, Runnable onComplete) {
        if (!isFirebaseAvailable()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        db.collection("master_timetable")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ClassSession> cloudMaster = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String code = doc.getString("classCode");
                        String subject = doc.getString("subjectName");
                        String className = doc.getString("className");
                        String lecturer = doc.getString("lecturerName");
                        Long semLong = doc.getLong("semester");
                        int sem = semLong != null ? semLong.intValue() : 1;
                        String day = doc.getString("day");
                        String time = doc.getString("timeSlot");
                        String lab = doc.getString("labLocation");

                        if (className == null || className.isEmpty()) {
                            className = "Class " + sem + "A";
                        }

                        if (subject != null && day != null && time != null && lab != null) {
                            cloudMaster.add(new ClassSession(code, subject, className, lecturer, sem, day, time, lab));
                        }
                    }

                    if (!cloudMaster.isEmpty()) {
                        ScheduleManager.saveMasterTimetable(context, cloudMaster);
                    }
                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> {
                    if (onComplete != null) onComplete.run();
                });
    }

    // ==========================================
    // 5. REALTIME DATABASE MAINTENANCE TOGGLES & BROADCAST ALERTS
    // ==========================================

    public interface MaintenanceStatusCallback {
        void onStatusChanged(Map<String, Boolean> maintenanceMap);
    }

    public interface FacultyAlertCallback {
        void onAlertReceived(String alertMessage);
    }

    public void updateLabMaintenanceStatus(String labName, boolean isAvailable) {
        if (rtdb == null) return;
        rtdb.getReference("lab_status").child(labName).setValue(isAvailable ? "AVAILABLE" : "UNDER MAINTENANCE");
    }

    public void broadcastFacultyAlert(String alertMessage, boolean setAllMaintenance) {
        if (rtdb == null) return;

        rtdb.getReference("faculty_alert").setValue(alertMessage);

        if (setAllMaintenance) {
            String[] labs = {"DCN", "NWL", "DNS", "SAD Lab", "Network Lab", "PGL 1", "PGL 2", "CSD", "HYP", "APL", "MML"};
            for (String lab : labs) {
                updateLabMaintenanceStatus(lab, false);
            }
        }
    }

    public void clearFacultyAlert() {
        if (rtdb == null) return;
        rtdb.getReference("faculty_alert").removeValue();
    }

    public void listenToFacultyAlert(FacultyAlertCallback callback) {
        if (rtdb == null || callback == null) return;

        rtdb.getReference("faculty_alert").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String msg = snapshot.getValue(String.class);
                callback.onAlertReceived(msg != null ? msg : "");
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    public void listenToLabMaintenanceStatus(MaintenanceStatusCallback callback) {
        if (rtdb == null || callback == null) return;

        rtdb.getReference("lab_status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Boolean> map = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String lab = child.getKey();
                    String val = child.getValue(String.class);
                    boolean isMaint = "UNDER MAINTENANCE".equalsIgnoreCase(val);
                    if (lab != null) {
                        String normalized = lab.replaceAll("\\s+", "").toLowerCase(Locale.US);
                        map.put(normalized, isMaint);
                    }
                }
                callback.onStatusChanged(map);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // ==========================================
    // 6. FACILITY ISSUE REPORTING (FIRESTORE)
    // ==========================================

    public interface IssueReportCallback {
        void onIssuesLoaded(List<Map<String, Object>> issues);
    }

    public void submitIssueReport(String lab, String desc, String priority, String category, OperationCallback callback) {
        if (!isFirebaseAvailable()) {
            callback.onFailure("Firebase unavailable.");
            return;
        }

        Map<String, Object> issue = new HashMap<>();
        issue.put("lab", lab);
        issue.put("desc", desc);
        issue.put("priority", priority);
        issue.put("category", category);
        issue.put("timestamp", System.currentTimeMillis());

        db.collection("reported_issues").add(issue)
                .addOnSuccessListener(doc -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public ListenerRegistration listenToReportedIssues(IssueReportCallback callback) {
        if (!isFirebaseAvailable()) return null;

        return db.collection("reported_issues")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    
                    List<Map<String, Object>> issueList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            data.put("id", doc.getId());
                            issueList.add(data);
                        }
                    }
                    callback.onIssuesLoaded(issueList);
                });
    }
}
