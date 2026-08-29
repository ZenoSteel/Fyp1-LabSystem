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
        masterTimetable = ScheduleManager.getMasterTimetable();
        studentCurrentSchedule = ScheduleManager.getStudentSchedule(this);
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
            tv.setText(getString(R.string.msg_no_gaps));
            tv.setTextColor(Color.RED);
            tv.setPadding(32, 32, 32, 32);
            layoutSuggestions.addView(tv);
            return;
        }

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

            TextView tvTitle = new TextView(this);
            tvTitle.setText(slot.getSubjectName());
            tvTitle.setTextSize(18);
            tvTitle.setTransformationMethod(null);
            tvTitle.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.campus_navy));
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvDetails = new TextView(this);
            tvDetails.setText(slot.getDay() + " | " + slot.getTimeSlot() + "\nLocation: " + slot.getLabLocation());
            tvDetails.setTextSize(14);
            tvDetails.setPadding(0, 8, 0, 8);

            innerLayout.addView(tvTitle);
            innerLayout.addView(tvDetails);
            
            card.addView(innerLayout);
            layoutSuggestions.addView(card);
        }
    }
}