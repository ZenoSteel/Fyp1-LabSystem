package com.example.fyp1;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

/**
 * Activity for students/lecturers to report lab issues.
 * Features an AI keyword classifier for automated priority tagging.
 */
public class ReportIssueActivity extends AppCompatActivity {

    private String currentLab;
    private EditText etComment;
    private CardView cardResult;
    private TextView tvPriority, tvCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        currentLab = getIntent().getStringExtra("LAB_NAME");
        if (currentLab == null) currentLab = "Unknown Lab";

        TextView tvTitle = findViewById(R.id.tvReportLabName);
        tvTitle.setText("Lab: " + currentLab);

        etComment = findViewById(R.id.etIssueComment);
        cardResult = findViewById(R.id.cardAiResult);
        tvPriority = findViewById(R.id.tvAiPriority);
        tvCategory = findViewById(R.id.tvAiCategory);
        Button btnSubmit = findViewById(R.id.btnSubmitReport);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> processAIAnalysis());
    }

    private void processAIAnalysis() {
        String comment = etComment.getText().toString().trim().toLowerCase();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Please describe the issue first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate "Thinking" time for the AI
        Toast.makeText(this, "AI analyzing report...", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            performKeywordClassification(comment);
        }, 1500);
    }

    private void performKeywordClassification(String text) {
        String priority = "LOW";
        String category = "General/Other";

        // 1. Priority Logic (Heuristic Rule-Based)
        if (text.contains("leak") || text.contains("smoke") || text.contains("fire") || 
            text.contains("broken") || text.contains("danger") || text.contains("locked")) {
            priority = "HIGH";
        } else if (text.contains("flicker") || text.contains("slow") || text.contains("wifi") || text.contains("internet")) {
            priority = "MEDIUM";
        }

        // 2. Category Logic
        if (text.contains("aircon") || text.contains("light") || text.contains("door") || text.contains("ceiling")) {
            category = "Facility/Infrastructure";
        } else if (text.contains("pc") || text.contains("monitor") || text.contains("mouse") || text.contains("keyboard")) {
            category = "Hardware/Peripherals";
        } else if (text.contains("software") || text.contains("login") || text.contains("error")) {
            category = "Software/System";
        }

        // Update UI
        tvPriority.setText(getString(R.string.label_priority) + priority);
        tvCategory.setText(getString(R.string.label_category) + category);
        
        if (priority.equals("HIGH")) {
            tvPriority.setTextColor(Color.RED);
        } else {
            tvPriority.setTextColor(ContextCompat.getColor(this, R.color.campus_blue));
        }

        cardResult.setVisibility(View.VISIBLE);
        
        final String finalPriority = priority;
        DatabaseManager.getInstance().submitIssueReport(currentLab, text, priority, category, new DatabaseManager.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ReportIssueActivity.this, getString(R.string.msg_report_success, finalPriority), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ReportIssueActivity.this, "Failed to submit report.", Toast.LENGTH_LONG).show();
            }
        });
    }
}