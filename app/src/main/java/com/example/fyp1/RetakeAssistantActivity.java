package com.example.fyp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * AI-Driven Academic Conflict Resolver for students retaking subjects.
 * Automatically filters previous semester subjects (e.g. Sem 1, 2, 3 for a Sem 4 student).
 */
public class RetakeAssistantActivity extends AppCompatActivity {

    private Spinner spinnerSubjects;
    private LinearLayout layoutSuggestions;
    private TextView tvHeader;
    private List<ClassSession> masterTimetable;
    private List<ClassSession> studentCurrentSchedule;
    private int studentSemester = 4; // Default to Sem 4 for DIT4A

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retake_assistant);

        spinnerSubjects = findViewById(R.id.spinnerRetakeSubject);
        layoutSuggestions = findViewById(R.id.layoutSuggestions);
        tvHeader = findViewById(R.id.tvSuggestionsHeader);
        Button btnAnalyze = findViewById(R.id.btnAnalyze);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        setupMockData();
        setupSpinner();

        btnAnalyze.setOnClickListener(v -> runAIAnalysis());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMockData();
        setupSpinner();
    }

    private void setupMockData() {
        masterTimetable = ScheduleManager.getMasterTimetable(this);
        studentCurrentSchedule = ScheduleManager.getStudentSchedule(this);
        studentSemester = detectStudentSemester();
    }

    private int detectStudentSemester() {
        // Detect semester from enrolled classes or registered class name (e.g. DIT4A -> Sem 4)
        for (ClassSession cs : studentCurrentSchedule) {
            if (cs.getSemester() > 0) {
                return cs.getSemester();
            }
        }
        return 4; // Default to Sem 4
    }

    private void setupSpinner() {
        List<String> subjectNames = new ArrayList<>();
        String allRetakeHeader = "🎓 All Retake Subjects (Semesters 1 - " + Math.max(1, studentSemester - 1) + ")";
        subjectNames.add(allRetakeHeader);

        for (ClassSession session : masterTimetable) {
            // Include subjects from previous semesters as retake candidates
            if (session.getSemester() < studentSemester) {
                if (!subjectNames.contains(session.getSubjectName())) {
                    subjectNames.add(session.getSubjectName());
                }
            }
        }

        // If no previous semester subjects found, include all master subjects
        if (subjectNames.size() == 1) {
            for (ClassSession session : masterTimetable) {
                if (!subjectNames.contains(session.getSubjectName())) {
                    subjectNames.add(session.getSubjectName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectNames);
        spinnerSubjects.setAdapter(adapter);
    }

    private void runAIAnalysis() {
        layoutSuggestions.removeAllViews();
        if (spinnerSubjects.getSelectedItem() == null) return;
        String selectedSubject = spinnerSubjects.getSelectedItem().toString();

        List<ClassSession> availableSlots = new ArrayList<>();
        boolean isAllMode = selectedSubject.startsWith("🎓 All Retake Subjects");

        // Pass 1: Strict semester filter (targetSession.getSemester() < studentSemester)
        for (ClassSession targetSession : masterTimetable) {
            boolean isPreviousSemester = (targetSession.getSemester() < studentSemester);

            if (isAllMode && !isPreviousSemester && studentSemester > 1) {
                continue; // Skip current or future semester subjects in Pass 1
            }

            if (isAllMode || targetSession.getSubjectName().equalsIgnoreCase(selectedSubject)) {
                boolean hasClash = false;
                for (ClassSession currentSession : studentCurrentSchedule) {
                    if (targetSession.getDay().equalsIgnoreCase(currentSession.getDay())) {
                        if (BookingUtils.isOverlap(targetSession.getTimeSlot(), currentSession.getTimeSlot())) {
                            hasClash = true;
                            break;
                        }
                    }
                }

                if (!hasClash) {
                    availableSlots.add(targetSession);
                }
            }
        }

        // Pass 2 Fallback: If no slots found under strict semester filter, evaluate ALL master subjects for clash-free slots
        if (availableSlots.isEmpty() && isAllMode) {
            for (ClassSession targetSession : masterTimetable) {
                boolean hasClash = false;
                for (ClassSession currentSession : studentCurrentSchedule) {
                    if (targetSession.getDay().equalsIgnoreCase(currentSession.getDay())) {
                        if (BookingUtils.isOverlap(targetSession.getTimeSlot(), currentSession.getTimeSlot())) {
                            hasClash = true;
                            break;
                        }
                    }
                }

                if (!hasClash) {
                    availableSlots.add(targetSession);
                }
            }
        }

        displayResults(availableSlots);
    }

    private void displayResults(List<ClassSession> slots) {
        tvHeader.setVisibility(View.VISIBLE);
        
        if (slots.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText(getString(R.string.msg_no_gaps));
            tv.setTextColor(Color.RED);
            tv.setPadding(32, 32, 32, 32);
            layoutSuggestions.addView(tv);
            return;
        }

        int optionCount = 1;
        for (ClassSession slot : slots) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            card.setLayoutParams(params);
            card.setRadius(16f);
            card.setCardElevation(6f);
            card.setCardBackgroundColor(Color.WHITE);

            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            innerLayout.setPadding(40, 40, 40, 40);

            // Badge / Option Header
            TextView tvOptionBadge = new TextView(this);
            tvOptionBadge.setText("Retake Option #" + optionCount + " • Semester " + slot.getSemester() + " (" + (slot.getClassCode().isEmpty() ? "DFC501" : slot.getClassCode()) + ")");
            tvOptionBadge.setTextSize(12);
            tvOptionBadge.setTextColor(Color.parseColor("#0284C7"));
            tvOptionBadge.setTypeface(null, android.graphics.Typeface.BOLD);

            // Subject Title
            TextView tvTitle = new TextView(this);
            tvTitle.setText(slot.getSubjectName());
            tvTitle.setTextSize(18);
            tvTitle.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.campus_navy));
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

            // Lecturer & Schedule Details
            TextView tvDetails = new TextView(this);
            String lecturer = slot.getLecturerName().isEmpty() ? "Faculty Staff" : slot.getLecturerName();
            String detailText = "👨‍🏫 Lecturer: " + lecturer + "\n" +
                                "📅 " + slot.getDay() + " (" + slot.getTimeSlot() + ")\n" +
                                "📍 Location: " + slot.getLabLocation();
            tvDetails.setText(detailText);
            tvDetails.setTextSize(14);
            tvDetails.setPadding(0, 8, 0, 16);
            tvDetails.setTextColor(Color.parseColor("#334155"));

            // Action Button: Select This Option
            Button btnSelect = new Button(this);
            btnSelect.setText("➕ Enroll in Retake Option #" + optionCount);
            btnSelect.setTextSize(13);
            btnSelect.setAllCaps(false);
            btnSelect.setBackgroundColor(Color.parseColor("#0284C7"));
            btnSelect.setTextColor(Color.WHITE);
            final ClassSession selectedSession = slot;
            btnSelect.setOnClickListener(v -> confirmSelectRetakeOption(selectedSession));

            innerLayout.addView(tvOptionBadge);
            innerLayout.addView(tvTitle);
            innerLayout.addView(tvDetails);
            innerLayout.addView(btnSelect);
            
            card.addView(innerLayout);
            layoutSuggestions.addView(card);

            optionCount++;
        }
    }

    private void confirmSelectRetakeOption(ClassSession session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Retake Enrollment");
        builder.setMessage("Do you want to add retake subject " + session.getSubjectName() + " (Sem " + session.getSemester() + ", " + session.getDay() + " " + session.getTimeSlot() + ") to your schedule?");
        
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            addSessionToMySchedule(session);
            Toast.makeText(this, session.getSubjectName() + " added to your schedule!", Toast.LENGTH_LONG).show();
            setupMockData();
            runAIAnalysis(); // Refresh analysis
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void addSessionToMySchedule(ClassSession session) {
        SharedPreferences prefs = getSharedPreferences("MasterScheduleData", Context.MODE_PRIVATE);
        String json = prefs.getString("my_schedule", "[]");
        try {
            JSONArray array = new JSONArray(json);
            JSONObject obj = new JSONObject();
            obj.put("classCode", session.getClassCode());
            obj.put("subjectName", session.getSubjectName());
            obj.put("className", session.getClassName());
            obj.put("lecturerName", session.getLecturerName());
            obj.put("semester", session.getSemester());
            obj.put("day", session.getDay());
            obj.put("timeSlot", session.getTimeSlot());
            obj.put("labLocation", session.getLabLocation());
            array.put(obj);
            prefs.edit().putString("my_schedule", array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
