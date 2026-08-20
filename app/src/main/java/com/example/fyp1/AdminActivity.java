package com.example.fyp1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * Administrative Command Center Activity.
 * Displays AI-prioritized issues and faculty-wide controls.
 */
public class AdminActivity extends AppCompatActivity {

    private LinearLayout layoutIssueFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        layoutIssueFeed = findViewById(R.id.layoutAdminIssues);
        ImageButton btnLogout = findViewById(R.id.btnLogoutAdmin);

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        findViewById(R.id.btnBroadcast).setOnClickListener(v -> 
            Toast.makeText(this, "Broadcasting notification to all users...", Toast.LENGTH_SHORT).show()
        );

        loadAIInsights();
    }

    private void loadAIInsights() {
        // Mocking AI-Classified Data for the Demo
        addIssueCard("DCN", "HIGH", "Facility", "Ceiling leak reported near server rack.");
        addIssueCard("Lab 3", "MEDIUM", "Hardware", "PC #15 mouse is unresponsive.");
        addIssueCard("NWL", "LOW", "Software", "Need Visual Studio updated to 2024.");
    }

    private void addIssueCard(String lab, String priority, String category, String desc) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setRadius(12f);
        card.setCardElevation(2f);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView tvHeader = new TextView(this);
        tvHeader.setText("[" + priority + "] " + lab + " - " + category);
        tvHeader.setTextSize(14);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        
        if (priority.equals("HIGH")) {
            tvHeader.setTextColor(Color.RED);
        } else if (priority.equals("MEDIUM")) {
            tvHeader.setTextColor(Color.parseColor("#EAB308")); // Amber
        } else {
            tvHeader.setTextColor(Color.parseColor("#3B82F6")); // Blue
        }

        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextSize(14);
        tvDesc.setTextColor(Color.DKGRAY);
        tvDesc.setPadding(0, 8, 0, 0);

        layout.addView(tvHeader);
        layout.addView(tvDesc);
        card.addView(layout);
        layoutIssueFeed.addView(card);
    }
}