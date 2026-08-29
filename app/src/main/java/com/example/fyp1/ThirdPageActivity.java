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
import androidx.cardview.widget.CardView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Redesigned Facility Booking screen.
 * Optimized for stability with empty and legacy data.
 */
public class ThirdPageActivity extends AppCompatActivity {

    private static final String TAG = "ThirdPageActivity";

    private TextView tvNavTitle, tvOccupiedLecturer, tvOccupiedSubject, tvOccupiedTime, tvRecommendationMsg, tvTotalDuration;
    private TextView tvThisWeek, tvNextWeek, tvPrevWeek, tvNextWeekArrow;
    private Spinner spinnerStartTime, spinnerEndTime;
    private CardView cardOccupied, cardRecommendation;
    private Button btnConfirmBooking, btnAutoFill;
    private ChipGroup chipGroupDays;

    private String currentLabName = "DCN";
    private String selectedDay = "Monday";
    private int currentWeekOffset = 0;
    
    private final List<String> timeOptions = Arrays.asList(
            "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
            "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_page);

        initializeViews();
        setupLabData();
        setupListeners();
        setupDaySelector(); // Do this after listeners to avoid early triggers
        updateDayChipLabels();
        
        analyzeLabAvailability();
    }

    private void initializeViews() {
        tvNavTitle = findViewById(R.id.tvNavTitle);
        tvOccupiedLecturer = findViewById(R.id.tvOccupiedLecturer);
        tvOccupiedSubject = findViewById(R.id.tvOccupiedSubject);
        tvOccupiedTime = findViewById(R.id.tvOccupiedTime);
        tvRecommendationMsg = findViewById(R.id.tvRecommendationMsg);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        
        tvThisWeek = findViewById(R.id.tvThisWeek);
        tvNextWeek = findViewById(R.id.tvNextWeek);
        tvPrevWeek = findViewById(R.id.tvPrevWeek);
        tvNextWeekArrow = findViewById(R.id.tvNextWeekArrow);
        
        spinnerStartTime = findViewById(R.id.spinnerStartTime);
        spinnerEndTime = findViewById(R.id.spinnerEndTime);
        
        cardOccupied = findViewById(R.id.cardOccupied);
        cardRecommendation = findViewById(R.id.cardRecommendation);
        
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        btnAutoFill = findViewById(R.id.btnAutoFill);
        chipGroupDays = findViewById(R.id.chipGroupDays);
    }

    private void setupLabData() {
        String labName = getIntent().getStringExtra("LAB_NAME");
        if (labName != null) currentLabName = labName;
        if (tvNavTitle != null) tvNavTitle.setText(getString(R.string.title_lab_schedule, currentLabName));

        String userRole = getIntent().getStringExtra("USER_ROLE");
        if (getString(R.string.role_student).equals(userRole)) {
            if (btnConfirmBooking != null) btnConfirmBooking.setVisibility(View.GONE);
        }
    }

    private void setupDaySelector() {
        if (chipGroupDays == null) return;
        
        chipGroupDays.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedDay = parseDayFromChip(chip.getText().toString());
                    analyzeLabAvailability();
                    refreshBoundaries();
                }
            }
        });
        chipGroupDays.check(R.id.chipMon);
    }

    private void setupListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        Button btnReturn = findViewById(R.id.btnReturnMenu);
        if (btnReturn != null) btnReturn.setOnClickListener(v -> finish());

        Button btnReport = findViewById(R.id.btnReportIssue);
        if (btnReport != null) {
            btnReport.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReportIssueActivity.class);
                intent.putExtra("LAB_NAME", currentLabName);
                startActivity(intent);
            });
        }

        if (tvNextWeek != null) tvNextWeek.setOnClickListener(v -> switchWeek(1));
        if (tvNextWeekArrow != null) tvNextWeekArrow.setOnClickListener(v -> switchWeek(1));
        if (tvThisWeek != null) tvThisWeek.setOnClickListener(v -> switchWeek(0));
        if (tvPrevWeek != null) tvPrevWeek.setOnClickListener(v -> switchWeek(0));

        if (btnAutoFill != null) btnAutoFill.setOnClickListener(v -> performAutoFill());
        if (btnConfirmBooking != null) btnConfirmBooking.setOnClickListener(v -> processBooking());

        // Setup Spinner
        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeOptions.subList(0, timeOptions.size() - 1));
        spinnerStartTime.setAdapter(startAdapter);

        spinnerStartTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                enforceBoundaryLogic(timeOptions.get(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerEndTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDurationDisplay();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void switchWeek(int offset) {
        currentWeekOffset = offset;
        updateDayChipLabels();
        if (offset == 0) {
            tvThisWeek.setTypeface(null, android.graphics.Typeface.BOLD);
            tvThisWeek.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.campus_navy));
            tvNextWeek.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvNextWeek.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary));
        } else {
            tvNextWeek.setTypeface(null, android.graphics.Typeface.BOLD);
            tvNextWeek.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.campus_navy));
            tvThisWeek.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvThisWeek.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary));
        }
        analyzeLabAvailability();
        refreshBoundaries();
    }

    private void refreshBoundaries() {
        if (spinnerStartTime != null && spinnerStartTime.getSelectedItem() != null) {
            enforceBoundaryLogic(spinnerStartTime.getSelectedItem().toString());
        }
    }

    private void analyzeLabAvailability() {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String bookingsJson = prefs.getString("BOOKING_LIST", "[]");
        
        try {
            JSONArray bookings = new JSONArray(bookingsJson);
            JSONObject activeSession = null;
            
            for (int i = 0; i < bookings.length(); i++) {
                JSONObject b = bookings.getJSONObject(i);
                int bOffset = b.optInt("week_offset", 0);
                if (b.getString("lab").equals(currentLabName) && b.getString("day").equals(selectedDay) && bOffset == currentWeekOffset) {
                    activeSession = b;
                    break;
                }
            }

            if (activeSession != null) {
                cardOccupied.setVisibility(View.VISIBLE);
                tvOccupiedLecturer.setText(getString(R.string.label_lecturer, "Encik Nizam"));
                tvOccupiedSubject.setText(getString(R.string.label_subject, currentLabName + " Session"));
                
                String timeStr = activeSession.optString("time", "Unknown");
                String[] parts = timeStr.split(" - ");
                String start = parts[0];
                String end = (parts.length > 1) ? parts[1].split(" \\(")[0] : "End";
                tvOccupiedTime.setText(getString(R.string.label_time_range, start, end));
                
                int[] hours = BookingUtils.getStartEndHours(timeStr);
                if (hours != null && hours[1] < 17) {
                    showRecommendation(format24To12(hours[1]), "05:00 PM");
                } else {
                    cardRecommendation.setVisibility(View.GONE);
                }
            } else {
                cardOccupied.setVisibility(View.GONE);
                showRecommendation("08:00 AM", "05:00 PM");
            }

        } catch (Exception e) {
            Log.e(TAG, "Analysis Error", e);
        }
    }

    private void showRecommendation(String start, String end) {
        if (cardRecommendation == null) return;
        cardRecommendation.setVisibility(View.VISIBLE);
        tvRecommendationMsg.setText(getString(R.string.msg_next_free_window, start, end));
        int startH = BookingUtils.getStartEndHours(start + " - 11:59 PM")[0];
        String suggestEnd = format24To12(Math.min(startH + 2, 17));
        btnAutoFill.setText(getString(R.string.btn_auto_fill_template, start, suggestEnd));
        btnAutoFill.setTag(new String[]{start, suggestEnd});
    }

    private void enforceBoundaryLogic(String selectedStart) {
        if (selectedStart == null) return;
        int[] startRange = BookingUtils.getStartEndHours(selectedStart + " - 11:59 PM");
        if (startRange == null) return;
        
        int startHour = startRange[0];
        checkAndAdjustStartTime(startHour);

        List<String> validEnds = new ArrayList<>();
        int nextLimit = 17;

        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        try {
            JSONArray bookings = new JSONArray(prefs.getString("BOOKING_LIST", "[]"));
            for (int i = 0; i < bookings.length(); i++) {
                JSONObject b = bookings.getJSONObject(i);
                int bOffset = b.optInt("week_offset", 0);
                if (b.getString("lab").equals(currentLabName) && b.getString("day").equals(selectedDay) && bOffset == currentWeekOffset) {
                    int[] bRange = BookingUtils.getStartEndHours(b.getString("time"));
                    if (bRange != null && bRange[0] > startHour && bRange[0] < nextLimit) {
                        nextLimit = bRange[0];
                    }
                }
            }
        } catch (Exception ignored) {}

        for (int i = timeOptions.indexOf(selectedStart) + 1; i < timeOptions.size(); i++) {
            int[] endRange = BookingUtils.getStartEndHours("08:00 AM - " + timeOptions.get(i));
            if (endRange != null && endRange[1] <= nextLimit) {
                validEnds.add(timeOptions.get(i));
            } else break;
        }

        ArrayAdapter<String> endAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, validEnds);
        spinnerEndTime.setAdapter(endAdapter);
    }

    private void checkAndAdjustStartTime(int selectedHour) {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        try {
            JSONArray bookings = new JSONArray(prefs.getString("BOOKING_LIST", "[]"));
            for (int i = 0; i < bookings.length(); i++) {
                JSONObject b = bookings.getJSONObject(i);
                int bOffset = b.optInt("week_offset", 0);
                if (b.getString("lab").equals(currentLabName) && b.getString("day").equals(selectedDay) && bOffset == currentWeekOffset) {
                    int[] range = BookingUtils.getStartEndHours(b.getString("time"));
                    if (range != null && selectedHour >= range[0] && selectedHour < range[1]) {
                        String newStart = format24To12(range[1]);
                        Toast.makeText(this, getString(R.string.error_overlap_conflict, "Staff", currentLabName, newStart), Toast.LENGTH_LONG).show();
                        int index = timeOptions.indexOf(newStart);
                        if (index != -1) spinnerStartTime.setSelection(index);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void updateDurationDisplay() {
        try {
            if (spinnerStartTime.getSelectedItem() == null || spinnerEndTime.getSelectedItem() == null) return;
            String s = spinnerStartTime.getSelectedItem().toString();
            String e = spinnerEndTime.getSelectedItem().toString();
            int[] r1 = BookingUtils.getStartEndHours(s + " - 11:59 PM");
            int[] r2 = BookingUtils.getStartEndHours("08:00 AM - " + e);
            if (r1 != null && r2 != null) {
                tvTotalDuration.setText(getString(R.string.label_total_duration, (r2[1] - r1[0])));
            }
        } catch (Exception e) {
            tvTotalDuration.setText("");
        }
    }

    private void performAutoFill() {
        String[] times = (String[]) btnAutoFill.getTag();
        if (times != null && times.length >= 2) {
            int startIdx = timeOptions.indexOf(times[0]);
            if (startIdx != -1) {
                spinnerStartTime.setSelection(startIdx);
                spinnerStartTime.post(() -> {
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerEndTime.getAdapter();
                    if (adapter != null) {
                        for (int i = 0; i < adapter.getCount(); i++) {
                            if (adapter.getItem(i).equals(times[1])) {
                                spinnerEndTime.setSelection(i);
                                break;
                            }
                        }
                    }
                });
            }
        }
    }

    private void processBooking() {
        if (spinnerStartTime.getSelectedItem() == null || spinnerEndTime.getSelectedItem() == null) return;
        
        String start = spinnerStartTime.getSelectedItem().toString();
        String end = spinnerEndTime.getSelectedItem().toString();
        String finalTime = BookingUtils.formatProfessionalTime(start + " - " + end);

        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        try {
            JSONArray array = new JSONArray(prefs.getString("BOOKING_LIST", "[]"));
            JSONObject b = new JSONObject();
            b.put("lab", currentLabName);
            b.put("day", selectedDay);
            b.put("time", finalTime);
            b.put("week_offset", currentWeekOffset);
            array.put(b);
            
            prefs.edit().putString("BOOKING_LIST", array.toString()).apply();
            Toast.makeText(this, getString(R.string.msg_booking_added, currentLabName, selectedDay, finalTime), Toast.LENGTH_LONG).show();
            analyzeLabAvailability();
        } catch (Exception ignored) {}
    }

    private void updateDayChipLabels() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        if (currentWeekOffset == 1) cal.add(Calendar.DATE, 7);

        int[] chipIds = {R.id.chipMon, R.id.chipTue, R.id.chipWed, R.id.chipThu, R.id.chipFri};
        String[] dayShortNames = {"MON", "TUE", "WED", "THU", "FRI"};

        for (int i = 0; i < chipIds.length; i++) {
            Chip chip = findViewById(chipIds[i]);
            if (chip != null) {
                int dateNum = cal.get(Calendar.DAY_OF_MONTH);
                chip.setText(getString(R.string.chip_day_format, dayShortNames[i], dateNum));
                cal.add(Calendar.DATE, 1);
            }
        }
    }

    private String parseDayFromChip(String text) {
        if (text.contains("MON")) return "Monday";
        if (text.contains("TUE")) return "Tuesday";
        if (text.contains("WED")) return "Wednesday";
        if (text.contains("THU")) return "Thursday";
        if (text.contains("FRI")) return "Friday";
        return "Monday";
    }

    private String format24To12(int h) {
        if (h == 0) return "12:00 AM";
        if (h == 12) return "12:00 PM";
        if (h > 12) return String.format(Locale.US, "%02d:00 PM", h - 12);
        return String.format(Locale.US, "%02d:00 AM", h);
    }
}