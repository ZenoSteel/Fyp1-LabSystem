package com.example.fyp1;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

/**
 * AI-Driven Academic Conflict Resolver for students retaking subjects.
 */
public class RetakeAssistantActivity extends AppCompatActivity {

    private Spinner spinnerSubjects;
    private LinearLayout layoutSuggestions;
    private TextView tvHeader;
    private List<ClassSession> masterTimetable;
    private List<ClassSession> studentCurrentSchedule;

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

    private void setupMockData() {
        masterTimetable = new ArrayList<>();
        // Mock data for all Sem 1-4 subjects
        masterTimetable.add(new ClassSession("Programming 1", 1, "Monday", "8:00 AM - 11:00 AM", "Lab 1"));
        masterTimetable.add(new ClassSession("Programming 1", 1, "Wednesday", "2:00 PM - 5:00 PM", "Lab 2"));
        masterTimetable.add(new ClassSession("Database Systems", 2, "Tuesday", "10:00 AM - 12:00 PM", "DCN"));
        masterTimetable.add(new ClassSession("Networking", 3, "Thursday", "2:00 PM - 4:00 PM", "NWL"));
        masterTimetable.add(new ClassSession("Operating Systems", 4, "Friday", "9:00 AM - 12:00 PM", "MML"));

        // Mock data for Student's current Sem 5 schedule (Constraining factors)
        studentCurrentSchedule = new ArrayList<>();
        studentCurrentSchedule.add(new ClassSession("Project 1", 5, "Monday", "9:00 AM - 12:00 PM", "Lab 3"));
        studentCurrentSchedule.add(new ClassSession("Security", 5, "Thursday", "2:00 PM - 5:00 PM", "NWL"));
    }

    private void setupSpinner() {
        List<String> subjectNames = new ArrayList<>();
        for (ClassSession session : masterTimetable) {
            if (!subjectNames.contains(session.getSubjectName())) {
                subjectNames.add(session.getSubjectName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectNames);
        spinnerSubjects.setAdapter(adapter);
    }

    private void runAIAnalysis() {
        layoutSuggestions.removeAllViews();
        String selectedSubject = spinnerSubjects.getSelectedItem().toString();

        List<ClassSession> availableSlots = new ArrayList<>();

        // AI Filtering Logic: Find slots for target subject that don't clash with current schedule
        for (ClassSession targetSession : masterTimetable) {
            if (targetSession.getSubjectName().equals(selectedSubject)) {
                boolean hasClash = false;
                for (ClassSession currentSession : studentCurrentSchedule) {
                    if (targetSession.getDay().equals(currentSession.getDay())) {
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
            tv.setText(R.string.msg_no_gaps);
            tv.setTextColor(Color.RED);
            layoutSuggestions.addView(tv);
            return;
        }

        for (ClassSession slot : slots) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 16);
            card.setLayoutParams(params);
            card.setRadius(12f);
            card.setCardElevation(4f);

            TextView tv = new TextView(this);
            String text = getString(R.string.suggestion_item, slot.toString(), slot.getLabLocation());
            tv.setText(text);
            tv.setPadding(32, 32, 32, 32);
            tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.campus_navy));
            tv.setTextSize(14);

            card.addView(tv);
            layoutSuggestions.addView(card);
        }
    }
}