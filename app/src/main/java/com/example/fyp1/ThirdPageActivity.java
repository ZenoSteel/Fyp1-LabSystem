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
    private Button btnConfirmBooking, btnAutoFill, btnAdminAddSchedule, btnDeleteSession;
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
        btnAdminAddSchedule = findViewById(R.id.btnAdminAddSchedule);
        btnDeleteSession = findViewById(R.id.btnDeleteSession);
        btnAutoFill = findViewById(R.id.btnAutoFill);
        chipGroupDays = findViewById(R.id.chipGroupDays);
    }

    private void setupLabData() {
        String labName = getIntent().getStringExtra("LAB_NAME");
        if (labName != null) currentLabName = labName;
        if (tvNavTitle != null) tvNavTitle.setText(getString(R.string.title_lab_schedule, currentLabName));

        String userRole = getIntent().getStringExtra("USER_ROLE");

        // Role-based visibility
        if (getString(R.string.role_student).equals(userRole)) {
            // Students can only view schedule
            if (btnConfirmBooking != null) btnConfirmBooking.setVisibility(View.GONE);
            if (btnAdminAddSchedule != null) btnAdminAddSchedule.setVisibility(View.GONE);
        } else if (getString(R.string.role_admin).equals(userRole)) {
            // Admin can book AND manage master schedule
            if (btnConfirmBooking != null) btnConfirmBooking.setVisibility(View.VISIBLE);
            if (btnAdminAddSchedule != null) {
                btnAdminAddSchedule.setVisibility(View.VISIBLE);
                btnAdminAddSchedule.setOnClickListener(v -> showAddScheduleDialog());
            }
        } else {
            // Lecturers can book slots, but cannot edit master schedule
            if (btnConfirmBooking != null) btnConfirmBooking.setVisibility(View.VISIBLE);
            if (btnAdminAddSchedule != null) btnAdminAddSchedule.setVisibility(View.GONE);
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

        // Setup Spinners
        updateStartTimeSpinner();

        spinnerStartTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerStartTime.getSelectedItem() != null) {
                    enforceBoundaryLogic(sanitizeTime(spinnerStartTime.getSelectedItem().toString()));
                }
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
            enforceBoundaryLogic(sanitizeTime(spinnerStartTime.getSelectedItem().toString()));
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
                if (b.getString("lab").equals(currentLabName) && b.getString("day").equalsIgnoreCase(selectedDay) && bOffset == currentWeekOffset) {
                    activeSession = b;
                    break;
                }
            }

            // Check Master Timetable if no individual user booking is found
            ClassSession masterSession = null;
            if (activeSession == null) {
                List<ClassSession> masterList = ScheduleManager.getMasterTimetable(this);
                for (ClassSession cs : masterList) {
                    if (cs.getLabLocation().equalsIgnoreCase(currentLabName) && cs.getDay().equalsIgnoreCase(selectedDay)) {
                        masterSession = cs;
                        break;
                    }
                }
            }

            String userRole = getIntent().getStringExtra("USER_ROLE");
            boolean isAdmin = getString(R.string.role_admin).equals(userRole);

            if (activeSession != null) {
                cardOccupied.setVisibility(View.VISIBLE);
                String bookedBy = activeSession.optString("bookedBy", "Faculty Staff");
                tvOccupiedLecturer.setText(getString(R.string.label_lecturer, bookedBy));
                tvOccupiedSubject.setText(getString(R.string.label_subject, currentLabName + " Session"));
                
                String timeStr = activeSession.optString("time", "Unknown");
                String[] parts = timeStr.split(" - ");
                String start = parts[0];
                tvOccupiedTime.setText(getString(R.string.label_time_range, start, (parts.length > 1 ? parts[1] : "")));

                if (isAdmin && btnDeleteSession != null) {
                    btnDeleteSession.setVisibility(View.VISIBLE);
                    final JSONObject sessionToRemove = activeSession;
                    btnDeleteSession.setOnClickListener(v -> confirmDeleteUserBooking(sessionToRemove));
                } else if (btnDeleteSession != null) {
                    btnDeleteSession.setVisibility(View.GONE);
                }

            } else if (masterSession != null) {
                cardOccupied.setVisibility(View.VISIBLE);
                String lecturer = masterSession.getLecturerName().isEmpty() ? "Faculty Schedule" : masterSession.getLecturerName();
                String code = masterSession.getClassCode().isEmpty() ? "" : masterSession.getClassCode() + " - ";
                tvOccupiedLecturer.setText(getString(R.string.label_lecturer, lecturer));
                tvOccupiedSubject.setText(getString(R.string.label_subject, code + masterSession.getSubjectName()));
                tvOccupiedTime.setText(getString(R.string.label_time_range, masterSession.getTimeSlot(), ""));

                if (isAdmin && btnDeleteSession != null) {
                    btnDeleteSession.setVisibility(View.VISIBLE);
                    final ClassSession csToRemove = masterSession;
                    btnDeleteSession.setOnClickListener(v -> confirmDeleteMasterSession(csToRemove));
                } else if (btnDeleteSession != null) {
                    btnDeleteSession.setVisibility(View.GONE);
                }

            } else {
                cardOccupied.setVisibility(View.GONE);
                if (btnDeleteSession != null) btnDeleteSession.setVisibility(View.GONE);
            }

            // Accurate AI Recommendation Calculation
            List<int[]> occupiedRanges = getOccupiedRangesForCurrentSelection();
            int[] freeGap = findEarliestFreeWindow(occupiedRanges);

            if (freeGap != null) {
                String gapStart = format24To12(freeGap[0]);
                String gapEnd = format24To12(freeGap[1]);
                showRecommendation(gapStart, gapEnd);
            } else {
                if (cardRecommendation != null) {
                    cardRecommendation.setVisibility(View.GONE);
                }
            }

            updateStartTimeSpinner();

        } catch (Exception e) {
            Log.e(TAG, "Analysis Error", e);
        }
    }

    private void showAddScheduleDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_add_schedule_title);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final android.widget.EditText etCode = new android.widget.EditText(this);
        etCode.setHint(R.string.hint_class_code);
        layout.addView(etCode);

        final android.widget.EditText etSubject = new android.widget.EditText(this);
        etSubject.setHint(R.string.hint_subject_name);
        layout.addView(etSubject);

        final android.widget.EditText etLecturer = new android.widget.EditText(this);
        etLecturer.setHint(R.string.hint_lecturer_name);
        layout.addView(etLecturer);

        final android.widget.EditText etSem = new android.widget.EditText(this);
        etSem.setHint(R.string.hint_semester);
        etSem.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etSem);

        final Spinner spDay = new Spinner(this);
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, 
                new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"});
        spDay.setAdapter(dayAdapter);
        layout.addView(spDay);

        final Spinner spStart = new Spinner(this);
        spStart.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeOptions.subList(0, timeOptions.size() - 1)));
        layout.addView(spStart);

        final Spinner spEnd = new Spinner(this);
        spEnd.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeOptions.subList(1, timeOptions.size())));
        layout.addView(spEnd);

        builder.setView(layout);

        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            String code = etCode.getText().toString().trim();
            String subject = etSubject.getText().toString().trim();
            String lecturer = etLecturer.getText().toString().trim();
            String semStr = etSem.getText().toString().trim();

            if (code.isEmpty()) code = "DFC501";
            if (subject.isEmpty()) subject = "Academic Lecture";
            if (lecturer.isEmpty()) lecturer = "Faculty Staff";
            int sem = semStr.isEmpty() ? 1 : Integer.parseInt(semStr);

            String day = spDay.getSelectedItem().toString();
            String startTime = spStart.getSelectedItem().toString();
            String endTime = spEnd.getSelectedItem().toString();
            String timeSlot = startTime + " - " + endTime;

            ClassSession session = new ClassSession(code, subject, lecturer, sem, day, timeSlot, currentLabName);
            ScheduleManager.addTimetableSession(this, session);

            Toast.makeText(this, R.string.msg_schedule_added, Toast.LENGTH_SHORT).show();
            analyzeLabAvailability();
        });

        builder.setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmDeleteMasterSession(ClassSession session) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_confirm_delete_title);
        builder.setMessage(getString(R.string.dialog_confirm_delete_msg, session.getSubjectName(), session.getDay()));
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            ScheduleManager.removeTimetableSession(this, session);
            Toast.makeText(this, R.string.msg_session_removed, Toast.LENGTH_SHORT).show();
            analyzeLabAvailability();
        });
        builder.setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmDeleteUserBooking(JSONObject bookingObj) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_confirm_delete_title);
        builder.setMessage(getString(R.string.dialog_confirm_delete_msg, currentLabName + " Booking", selectedDay));
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
            try {
                JSONArray oldBookings = new JSONArray(prefs.getString("BOOKING_LIST", "[]"));
                JSONArray newBookings = new JSONArray();
                for (int i = 0; i < oldBookings.length(); i++) {
                    JSONObject b = oldBookings.getJSONObject(i);
                    int bOffset = b.optInt("week_offset", 0);
                    if (b.getString("lab").equalsIgnoreCase(currentLabName) && 
                        b.getString("day").equalsIgnoreCase(selectedDay) && 
                        bOffset == currentWeekOffset) {
                        continue; // Remove target booking
                    }
                    newBookings.put(b);
                }
                prefs.edit().putString("BOOKING_LIST", newBookings.toString()).apply();
                Toast.makeText(this, R.string.msg_session_removed, Toast.LENGTH_SHORT).show();
                analyzeLabAvailability();
            } catch (Exception ignored) {}
        });
        builder.setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private int[] findEarliestFreeWindow(List<int[]> occupied) {
        java.util.Collections.sort(occupied, (a, b) -> Integer.compare(a[0], b[0]));

        List<int[]> merged = new ArrayList<>();
        for (int[] range : occupied) {
            if (merged.isEmpty()) {
                merged.add(new int[]{range[0], range[1]});
            } else {
                int[] last = merged.get(merged.size() - 1);
                if (range[0] <= last[1]) {
                    last[1] = Math.max(last[1], range[1]);
                } else {
                    merged.add(new int[]{range[0], range[1]});
                }
            }
        }

        int currentPointer = 8;
        for (int[] occ : merged) {
            if (occ[0] > currentPointer) {
                int gapStart = currentPointer;
                int gapEnd = Math.min(occ[0], 17);
                if (gapEnd > gapStart) {
                    return new int[]{gapStart, gapEnd};
                }
            }
            currentPointer = Math.max(currentPointer, occ[1]);
        }

        if (currentPointer < 17) {
            return new int[]{currentPointer, 17};
        }

        return null; // Day is completely full
    }

    private void showRecommendation(String start, String end) {
        if (cardRecommendation == null) return;
        cardRecommendation.setVisibility(View.VISIBLE);
        tvRecommendationMsg.setText(getString(R.string.msg_next_free_window, start, end));
        int startH = BookingUtils.getStartEndHours(start + " - 11:59 PM")[0];
        int endH = BookingUtils.getStartEndHours("08:00 AM - " + end)[1];
        int autoFillEndH = Math.min(startH + 2, endH);
        String suggestEnd = format24To12(autoFillEndH);
        btnAutoFill.setText(getString(R.string.btn_auto_fill_template, start, suggestEnd));
        btnAutoFill.setTag(new String[]{start, suggestEnd});
    }

    private String sanitizeTime(String raw) {
        if (raw == null) return "";
        return raw.replace(" (Occupied)", "").replace(" (Unavailable)", "").trim();
    }

    private List<int[]> getOccupiedRangesForCurrentSelection() {
        List<int[]> ranges = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        try {
            JSONArray bookings = new JSONArray(prefs.getString("BOOKING_LIST", "[]"));
            for (int i = 0; i < bookings.length(); i++) {
                JSONObject b = bookings.getJSONObject(i);
                int bOffset = b.optInt("week_offset", 0);
                if (b.getString("lab").equalsIgnoreCase(currentLabName) && 
                    b.getString("day").equalsIgnoreCase(selectedDay) && 
                    bOffset == currentWeekOffset) {
                    
                    int[] r = BookingUtils.getStartEndHours(b.getString("time"));
                    if (r != null && r[1] > r[0]) {
                        ranges.add(r);
                    }
                }
            }
        } catch (Exception ignored) {}

        List<ClassSession> masterList = ScheduleManager.getMasterTimetable(this);
        for (ClassSession cs : masterList) {
            if (cs.getLabLocation().equalsIgnoreCase(currentLabName) && 
                cs.getDay().equalsIgnoreCase(selectedDay)) {
                
                int[] r = BookingUtils.getStartEndHours(cs.getTimeSlot());
                if (r != null && r[1] > r[0]) {
                    ranges.add(r);
                }
            }
        }

        return ranges;
    }

    private void updateStartTimeSpinner() {
        if (spinnerStartTime == null) return;

        List<String> displayOptions = new ArrayList<>();
        List<int[]> occupiedRanges = getOccupiedRangesForCurrentSelection();

        List<String> rawStartOptions = timeOptions.subList(0, timeOptions.size() - 1);
        for (String rawTime : rawStartOptions) {
            int[] range = BookingUtils.getStartEndHours(rawTime + " - 11:59 PM");
            int hour = (range != null) ? range[0] : -1;

            boolean isOccupied = false;
            for (int[] occ : occupiedRanges) {
                if (hour >= occ[0] && hour < occ[1]) {
                    isOccupied = true;
                    break;
                }
            }

            if (isOccupied) {
                displayOptions.add(rawTime + " (Occupied)");
            } else {
                displayOptions.add(rawTime);
            }
        }

        String currentSelected = (spinnerStartTime.getSelectedItem() != null) ? 
                sanitizeTime(spinnerStartTime.getSelectedItem().toString()) : null;

        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayOptions);
        spinnerStartTime.setAdapter(startAdapter);

        if (currentSelected != null) {
            for (int i = 0; i < displayOptions.size(); i++) {
                if (sanitizeTime(displayOptions.get(i)).equals(currentSelected)) {
                    spinnerStartTime.setSelection(i);
                    break;
                }
            }
        }
    }

    private void enforceBoundaryLogic(String selectedStart) {
        if (selectedStart == null) return;
        String cleanStart = sanitizeTime(selectedStart);
        int[] startRange = BookingUtils.getStartEndHours(cleanStart + " - 11:59 PM");
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

        List<ClassSession> masterList = ScheduleManager.getMasterTimetable(this);
        for (ClassSession cs : masterList) {
            if (cs.getLabLocation().equalsIgnoreCase(currentLabName) && cs.getDay().equalsIgnoreCase(selectedDay)) {
                int[] bRange = BookingUtils.getStartEndHours(cs.getTimeSlot());
                if (bRange != null && bRange[0] > startHour && bRange[0] < nextLimit) {
                    nextLimit = bRange[0];
                }
            }
        }

        int cleanStartIndex = timeOptions.indexOf(cleanStart);
        if (cleanStartIndex != -1) {
            for (int i = cleanStartIndex + 1; i < timeOptions.size(); i++) {
                int[] endRange = BookingUtils.getStartEndHours("08:00 AM - " + timeOptions.get(i));
                if (endRange != null && endRange[1] <= nextLimit) {
                    validEnds.add(timeOptions.get(i));
                } else break;
            }
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
            String s = sanitizeTime(spinnerStartTime.getSelectedItem().toString());
            String e = sanitizeTime(spinnerEndTime.getSelectedItem().toString());
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
        
        String rawStart = spinnerStartTime.getSelectedItem().toString();
        String rawEnd = spinnerEndTime.getSelectedItem().toString();

        if (rawStart.contains("(Occupied)") || rawEnd.contains("(Occupied)")) {
            Toast.makeText(this, getString(R.string.error_clash_detected), Toast.LENGTH_LONG).show();
            return;
        }

        String start = sanitizeTime(rawStart);
        String end = sanitizeTime(rawEnd);
        String finalTime = BookingUtils.formatProfessionalTime(start + " - " + end);

        // Get actual user name for the booking record
        String username = getIntent().getStringExtra("USER_NAME");
        if (username == null || username.isEmpty()) username = "Authorized User";

        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        try {
            JSONArray array = new JSONArray(prefs.getString("BOOKING_LIST", "[]"));
            
            // FINAL SECURITY CHECK: Verify no clash exists before saving
            for (int i = 0; i < array.length(); i++) {
                JSONObject existing = array.getJSONObject(i);
                int bOffset = existing.optInt("week_offset", 0);
                
                if (existing.getString("lab").equals(currentLabName) && 
                    existing.getString("day").equals(selectedDay) && 
                    bOffset == currentWeekOffset) {
                    
                    if (BookingUtils.isOverlap(finalTime, existing.getString("time"))) {
                        Toast.makeText(this, getString(R.string.error_clash_detected), Toast.LENGTH_LONG).show();
                        return; // ABORT BOOKING
                    }
                }
            }

            JSONObject b = new JSONObject();
            b.put("lab", currentLabName);
            b.put("day", selectedDay);
            b.put("time", finalTime);
            b.put("week_offset", currentWeekOffset);
            b.put("bookedBy", username); // Save the real name!
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