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

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Administrative Command Center Activity.
 * Displays AI-prioritized issues and faculty-wide controls for all 11 labs.
 */
public class AdminActivity extends AppCompatActivity {

    private LinearLayout layoutIssueFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initializeUI();
        loadAIInsights();
    }

    private void initializeUI() {
        layoutIssueFeed = findViewById(R.id.layoutAdminIssues);
        ImageButton btnLogout = findViewById(R.id.btnLogoutAdmin);

        // Update Dynamic Metrics
        ((TextView)findViewById(R.id.tvActiveAlertsCount)).setText("3");
        ((TextView)findViewById(R.id.tvMaintenanceCount)).setText("1");
        ((TextView)findViewById(R.id.tvUsageCount)).setText("12");

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        findViewById(R.id.btnBroadcast).setOnClickListener(v -> showBroadcastAlertDialog());

        View btnTimetables = findViewById(R.id.btnManageTimetables);
        if (btnTimetables != null) {
            btnTimetables.setOnClickListener(v -> showLabPickerForTimetable());
        }

        // Setup All 11 Lab Switches
        setupLabSwitch(R.id.switchDCN, "DCN");
        setupLabSwitch(R.id.switchNWL, "NWL");
        setupLabSwitch(R.id.switchLab1, "DNS");
        setupLabSwitch(R.id.switchSAD, "SAD Lab");
        setupLabSwitch(R.id.switchNetwork, "Network Lab");
        setupLabSwitch(R.id.switchPGL1, "PGL 1");
        setupLabSwitch(R.id.switchPGL2, "PGL 2");
        setupLabSwitch(R.id.switchLab2, "CSD");
        setupLabSwitch(R.id.switchHYP, "HYP");
        setupLabSwitch(R.id.switchLab3, "APL");
        setupLabSwitch(R.id.switchMML, "MML");
    }

    private void showBroadcastAlertDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Broadcast Faculty Alert");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final android.widget.EditText etAlertMsg = new android.widget.EditText(this);
        etAlertMsg.setHint("e.g. ⚠️ EMERGENCY: Power outage! All labs closed until 5:00 PM.");
        layout.addView(etAlertMsg);

        final android.widget.CheckBox cbEmergencyMode = new android.widget.CheckBox(this);
        cbEmergencyMode.setText("🔴 Set ALL Labs to Under Maintenance (Red)");
        cbEmergencyMode.setPadding(0, 16, 0, 16);
        layout.addView(cbEmergencyMode);

        builder.setView(layout);

        builder.setPositiveButton("Broadcast Now", (dialog, which) -> {
            String msg = etAlertMsg.getText().toString().trim();
            if (msg.isEmpty()) msg = "⚠️ EMERGENCY FACULTY ALERT: All computer labs are temporarily unavailable due to system maintenance.";
            
            boolean setMaint = cbEmergencyMode.isChecked();
            DatabaseManager.getInstance().broadcastFacultyAlert(msg, setMaint);
            
            if (setMaint) {
                int[] switchIds = {R.id.switchDCN, R.id.switchNWL, R.id.switchLab1, R.id.switchSAD, R.id.switchNetwork, R.id.switchPGL1, R.id.switchPGL2, R.id.switchLab2, R.id.switchHYP, R.id.switchLab3, R.id.switchMML};
                for (int id : switchIds) {
                    SwitchMaterial sw = findViewById(id);
                    if (sw != null) sw.setChecked(false);
                }
            }

            Toast.makeText(this, "Broadcast alert sent to all users!", Toast.LENGTH_LONG).show();
        });

        builder.setNeutralButton("Clear Active Alert", (dialog, which) -> {
            DatabaseManager.getInstance().clearFacultyAlert();
            Toast.makeText(this, "Active broadcast alert cleared.", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void setupLabSwitch(int id, String labName) {
        SwitchMaterial sw = findViewById(id);
        if (sw != null) {
            sw.setOnCheckedChangeListener((btn, isChecked) -> {
                String status = isChecked ? "AVAILABLE" : "UNDER MAINTENANCE";
                Toast.makeText(this, labName + " status updated to: " + status, Toast.LENGTH_SHORT).show();
                DatabaseManager.getInstance().updateLabMaintenanceStatus(labName, isChecked);
            });
        }
    }

    private void showLabPickerForTimetable() {
        final String[] labs = {"DCN", "NWL", "DNS", "SAD Lab", "Network Lab", "PGL 1", "PGL 2", "CSD", "HYP", "APL", "MML"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Lab to Edit Timetable");
        builder.setItems(labs, (dialog, which) -> openLabTimetable(labs[which]));
        builder.setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void openLabTimetable(String labName) {
        Intent intent = new Intent(this, ThirdPageActivity.class);
        intent.putExtra("LAB_NAME", labName);
        intent.putExtra("USER_NAME", "Admin");
        intent.putExtra("USER_ROLE", getString(R.string.role_admin));
        startActivity(intent);
    }

    private void loadAIInsights() {
        addIssueCard("DCN", "HIGH", "Facility", "Ceiling leak reported near server rack.");
        addIssueCard("SAD Lab", "HIGH", "Hardware", "Smart Board touch sensor malfunctioning.");
        addIssueCard("APL", "MEDIUM", "Hardware", "PC #15 mouse is unresponsive.");
        addIssueCard("NWL", "LOW", "Software", "Need Visual Studio updated to 2024.");
    }

    private void addIssueCard(String lab, String priority, String category, String desc) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setRadius(16f);
        card.setCardElevation(2f);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView tvHeader = new TextView(this);
        tvHeader.setText("[" + priority + "] " + lab + " - " + category);
        tvHeader.setTextSize(14);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        
        if (priority.equals("HIGH")) {
            tvHeader.setTextColor(Color.parseColor("#EF4444"));
        } else if (priority.equals("MEDIUM")) {
            tvHeader.setTextColor(Color.parseColor("#F59E0B"));
        } else {
            tvHeader.setTextColor(Color.parseColor("#3B82F6"));
        }

        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextSize(14);
        tvDesc.setTextColor(Color.parseColor("#475569"));
        tvDesc.setPadding(0, 12, 0, 0);

        layout.addView(tvHeader);
        layout.addView(tvDesc);
        card.addView(layout);
        layoutIssueFeed.addView(card);
    }
}