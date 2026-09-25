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

    @Override
    protected void onResume() {
        super.onResume();
        if (DatabaseManager.getInstance().isFirebaseAvailable()) {
            DatabaseManager.getInstance().listenToLabMaintenanceStatus(map -> {
                int count = 0;
                for (Boolean isMaint : map.values()) {
                    if (Boolean.TRUE.equals(isMaint)) count++;
                }
                TextView tvMaint = findViewById(R.id.tvMaintenanceCount);
                if (tvMaint != null) {
                    tvMaint.setText(String.valueOf(count));
                }
            });
        }
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

        View tileHighAlerts = findViewById(R.id.cardHighAlertsTile);
        if (tileHighAlerts != null) {
            tileHighAlerts.setOnClickListener(v -> showTileDetailDialog(
                "🚨 Active High Priority AI Alerts",
                "• [DCN - Facility]: Ceiling leak reported near server rack.\n\n" +
                "• [SAD Lab - Hardware]: Smart Board touch sensor malfunctioning.\n\n" +
                "• [NWL - Critical Network]: Switch port 4 disconnected."
            ));
        }

        View tileMaint = findViewById(R.id.cardMaintenanceTile);
        if (tileMaint != null) {
            tileMaint.setOnClickListener(v -> showTileDetailDialog(
                "🛠️ Labs Currently Under Maintenance",
                "• DCN - Switched Off (Under Maintenance)\n\n" +
                "You can toggle maintenance status for any lab using the switches in the System Control Panel below."
            ));
        }

        View tileUsage = findViewById(R.id.cardUsageTile);
        if (tileUsage != null) {
            tileUsage.setOnClickListener(v -> showTileDetailDialog(
                "📊 Today's Faculty Lab Usage",
                "• Total Active Sessions Today: 12\n\n" +
                "• DCN: 2 Sessions (Server Admin / Security)\n" +
                "• DNS: 2 Sessions (Programming 1 / DB Systems)\n" +
                "• CSD: 2 Sessions (Web Architecture / Coding)\n" +
                "• APL: 2 Sessions (Project 1 / Labs)\n" +
                "• NWL: 2 Sessions (Networking / Wireless)\n" +
                "• MML: 2 Sessions (Multimedia / Design)"
            ));
        }

        View tileHealth = findViewById(R.id.cardAIHealthTile);
        if (tileHealth != null) {
            tileHealth.setOnClickListener(v -> showTileDetailDialog(
                "🟢 AI System Health & Status",
                "• AI Gap Engine: ONLINE (1-Tap Auto-Fill Active)\n" +
                "• NLP Chatbot Engine: ONLINE (Single-Message Ready)\n" +
                "• AI Retake Resolver: ONLINE (Sem 1-4 Matching)\n" +
                "• Firebase Realtime Sync: CONNECTED (Zero Latency)\n" +
                "• Cloud Firestore Database: CONNECTED"
            ));
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
        if (layoutIssueFeed != null) layoutIssueFeed.removeAllViews();

        ((TextView)findViewById(R.id.tvActiveAlertsCount)).setText("0");
        ((TextView)findViewById(R.id.tvUsageCount)).setText("0");

        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("🟢 All 11 Labs Operational • No Active Facility Issues Reported");
        tvEmpty.setTextSize(13f);
        tvEmpty.setTextColor(Color.parseColor("#15803D"));
        tvEmpty.setPadding(32, 32, 32, 32);
        layoutIssueFeed.addView(tvEmpty);
    }

    private void addIssueCard(String lab, String priority, String category, String desc) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setRadius(14f);
        card.setCardElevation(3f);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvBadge = new TextView(this);
        tvBadge.setTextSize(11f);
        tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        tvBadge.setPadding(16, 6, 16, 6);

        if ("HIGH".equalsIgnoreCase(priority)) {
            tvBadge.setText("🔴 HIGH PRIORITY");
            tvBadge.setTextColor(Color.parseColor("#991B1B"));
            tvBadge.setBackgroundResource(R.drawable.bg_status_occupied);
            card.setCardBackgroundColor(Color.parseColor("#FEF2F2"));
        } else if ("MEDIUM".equalsIgnoreCase(priority)) {
            tvBadge.setText("🟡 MEDIUM PRIORITY");
            tvBadge.setTextColor(Color.parseColor("#92400E"));
            tvBadge.setBackgroundColor(Color.parseColor("#FEF3C7"));
            card.setCardBackgroundColor(Color.parseColor("#FFFBEB"));
        } else {
            tvBadge.setText("🔵 LOW PRIORITY");
            tvBadge.setTextColor(Color.parseColor("#1E40AF"));
            tvBadge.setBackgroundColor(Color.parseColor("#DBEAFE"));
            card.setCardBackgroundColor(Color.parseColor("#EFF6FF"));
        }

        TextView tvLabTitle = new TextView(this);
        tvLabTitle.setText(" • " + lab + " (" + category + ")");
        tvLabTitle.setTextSize(14f);
        tvLabTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvLabTitle.setTextColor(Color.parseColor("#0F172A"));

        headerRow.addView(tvBadge);
        headerRow.addView(tvLabTitle);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextSize(13f);
        tvDesc.setTextColor(Color.parseColor("#334155"));
        tvDesc.setPadding(0, 12, 0, 0);

        layout.addView(headerRow);
        layout.addView(tvDesc);
        card.addView(layout);
        layoutIssueFeed.addView(card);
    }

    private void showTileDetailDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }
}