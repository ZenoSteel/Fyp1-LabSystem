package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ThirdPageActivity extends AppCompatActivity {

    private static final String TAG = "ThirdPageActivity";

    private TextView tvLabName;
    private Spinner spinnerTime, spinnerDay;
    private Button btnConfirmBooking;

    private String currentLabName = "DCN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_page);

        tvLabName = findViewById(R.id.tvLabName);
        spinnerTime = findViewById(R.id.spinnerTime);
        spinnerDay = findViewById(R.id.spinnerDay);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnReturnMenu = findViewById(R.id.btnReturnMenu);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        Button btnReportIssue = findViewById(R.id.btnReportIssue);

        String labName = getIntent().getStringExtra("LAB_NAME");
        if (labName != null && !labName.isEmpty()) {
            currentLabName = labName;
        }
        tvLabName.setText(currentLabName);

        String userRole = getIntent().getStringExtra("USER_ROLE");
        if (getString(R.string.role_student).equals(userRole)) {
            btnConfirmBooking.setVisibility(View.GONE);
        }

        // Populate time options customized for each lab
        setupSpinners(currentLabName);

        btnConfirmBooking.setOnClickListener(v -> saveBooking());
        btnReportIssue.setOnClickListener(v -> {
            Intent intent = new Intent(ThirdPageActivity.this, ReportIssueActivity.class);
            intent.putExtra("LAB_NAME", currentLabName);
            startActivity(intent);
        });
        btnBack.setOnClickListener(v -> finish());
        btnReturnMenu.setOnClickListener(v -> finish());
    }

    /**
     * Refreshes the time spinner to mark slots that are already booked for the selected day.
     */
    private void refreshTimeSpinner(String labName, String selectedDay) {
        String[] baseOptions = getBaseTimeOptions(labName);
        String[] displayOptions = new String[baseOptions.length];

        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String existingBookingsJson = prefs.getString("BOOKING_LIST", "[]");

        try {
            JSONArray bookingArray = new JSONArray(existingBookingsJson);

            for (int i = 0; i < baseOptions.length; i++) {
                String slot = baseOptions[i];
                boolean isOccupied = false;

                for (int j = 0; j < bookingArray.length(); j++) {
                    JSONObject booking = bookingArray.getJSONObject(j);
                    if (booking.getString("lab").equals(labName) && booking.getString("day").equals(selectedDay)) {
                        if (BookingUtils.isOverlap(booking.getString("time"), slot)) {
                            isOccupied = true;
                            break;
                        }
                    }
                }

                if (isOccupied) {
                    displayOptions[i] = slot + " [OCCUPIED]";
                } else {
                    displayOptions[i] = slot;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error checking occupied slots", e);
            displayOptions = baseOptions;
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, displayOptions);
        spinnerTime.setAdapter(timeAdapter);
    }

    private String[] getBaseTimeOptions(String labName) {
        switch (labName) {
            case "DCN":
            case "NWL":
                return new String[]{
                        "8:00 AM - 11:00 AM (3 hrs)",
                        "11:00 AM - 2:00 PM (3 hrs)",
                        "2:00 PM - 5:00 PM (3 hrs)"
                };
            case "Lab 1":
            case "Lab 2":
            case "Lab 3":
                return new String[]{
                        "8:00 AM - 10:00 AM (2 hrs)",
                        "10:00 AM - 12:00 PM (2 hrs)",
                        "2:00 PM - 4:00 PM (2 hrs)"
                };
            case "MML":
            case "HYP":
                return new String[]{
                        "9:00 AM - 12:00 PM (3 hrs)",
                        "1:00 PM - 3:00 PM (2 hrs)",
                        "3:00 PM - 5:00 PM (2 hrs)",
                        "Full Day (9:00 AM - 5:00 PM)"
                };
            default:
                return new String[]{
                        "8:00 AM - 10:00 AM (2 hrs)",
                        "11:00 AM - 2:00 PM (3 hrs)",
                        "2:00 PM - 5:00 PM (3 hrs)"
                };
        }
    }

    // ================= APPEND BOOKING TO JSON LIST =================
    private void saveBooking() {
        String selectedTimeRaw = spinnerTime.getSelectedItem().toString();
        
        if (selectedTimeRaw.contains("[OCCUPIED]")) {
            Toast.makeText(this, "This slot is already occupied!", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedDay = spinnerDay.getSelectedItem().toString();

        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String existingBookingsJson = prefs.getString("BOOKING_LIST", "[]");

        try {
            JSONArray bookingArray = new JSONArray(existingBookingsJson);

            // Double check Clash Detection (Safety)
            for (int i = 0; i < bookingArray.length(); i++) {
                JSONObject existing = bookingArray.getJSONObject(i);
                if (existing.getString("lab").equals(currentLabName) &&
                    existing.getString("day").equals(selectedDay)) {
                    
                    if (BookingUtils.isOverlap(existing.getString("time"), selectedTimeRaw)) {
                        Toast.makeText(this, getString(R.string.error_clash_detected), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            JSONObject newBooking = new JSONObject();
            newBooking.put("lab", currentLabName);
            newBooking.put("day", selectedDay);
            newBooking.put("time", selectedTimeRaw);

            bookingArray.put(newBooking);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("BOOKING_LIST", bookingArray.toString());
            editor.putBoolean("HAS_ACTIVE_BOOKING", true);
            editor.apply();

            String confirmationMessage = getString(R.string.msg_booking_added, currentLabName, selectedDay, selectedTimeRaw);
            Toast.makeText(this, confirmationMessage, Toast.LENGTH_LONG).show();
            
            // Refresh spinner immediately after successful booking
            refreshTimeSpinner(currentLabName, selectedDay);

        } catch (JSONException e) {
            Log.e(TAG, "Error saving booking", e);
            Toast.makeText(this, getString(R.string.error_saving_booking), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpinners(String labName) {
        String[] dayOptions = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, dayOptions);
        spinnerDay.setAdapter(dayAdapter);

        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshTimeSpinner(labName, dayOptions[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}