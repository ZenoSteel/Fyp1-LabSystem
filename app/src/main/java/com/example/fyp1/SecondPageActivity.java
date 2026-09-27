package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main Dashboard Activity.
 * Displays lab directory, live availability, and provides access to core AI features.
 */
public class SecondPageActivity extends AppCompatActivity {

    private static final String TAG = "SecondPageActivity";

    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private ImageView ivLabPhoto;
    private TextView tvUserGreeting;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_page);

        initializeViews();
        setupUserContext();
        setupLabCards();
        setupNavigationDrawer();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        ivLabPhoto = findViewById(R.id.ivLabPhoto);
        tvUserGreeting = findViewById(R.id.tvUserGreeting);
    }

    private void setupUserContext() {
        String username = getIntent().getStringExtra("USER_NAME");
        userRole = getIntent().getStringExtra("USER_ROLE");

        if (username != null && !username.isEmpty()) {
            String roleDisplay = userRole != null ? " (" + userRole + ")" : "";
            tvUserGreeting.setText(getString(R.string.user_greeting_placeholder, username.toUpperCase() + roleDisplay));
        }

        tvUserGreeting.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_NAME", username);
            intent.putExtra("USER_ROLE", userRole);
            startActivity(intent);
        });

        if (getString(R.string.role_student).equals(userRole)) {
            TextView menuMyBooking = findViewById(R.id.menuMyBooking);
            if (menuMyBooking != null) menuMyBooking.setText(R.string.menu_my_schedule);
            
            TextView menuRetake = findViewById(R.id.menuRetakeAssistant);
            if (menuRetake != null) {
                menuRetake.setVisibility(android.view.View.VISIBLE);
                menuRetake.setOnClickListener(v -> {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    startActivity(new Intent(this, RetakeAssistantActivity.class));
                });
            }
        }
    }

    private void setupLabCards() {
        // Ground Floor
        setupLabCard(R.id.cardDCN, "DCN");
        setupLabCard(R.id.cardNWL, "NWL");
        setupLabCard(R.id.cardLab1, "DNS");
        
        // First Floor
        setupLabCard(R.id.cardSAD, "SAD Lab");
        setupLabCard(R.id.cardNetwork, "Network Lab");
        
        // Second Floor
        setupLabCard(R.id.cardPGL2, "PGL 2");
        setupLabCard(R.id.cardLab2, "CSD");
        setupLabCard(R.id.cardPGL1, "PGL 1");
        
        // Third Floor
        setupLabCard(R.id.cardHYP, "HYP");
        setupLabCard(R.id.cardLab3, "APL");
        setupLabCard(R.id.cardMML, "MML");

        ivLabPhoto.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.toast_lab_photo_clicked), Toast.LENGTH_SHORT).show()
        );
    }

    private void setupNavigationDrawer() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        findViewById(R.id.menuMyBooking).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            showMyBookingDialog();
        });

        findViewById(R.id.menuLecturers).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, LecturerListActivity.class));
        });

        findViewById(R.id.menuAIChatbot).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(this, ChatbotActivity.class);
            intent.putExtra("USER_NAME", getIntent().getStringExtra("USER_NAME"));
            intent.putExtra("USER_ROLE", userRole);
            startActivity(intent);
        });

        findViewById(R.id.menuAboutUs).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AboutUsActivity.class));
        });
        findViewById(R.id.menuFAQ).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, FAQActivity.class));
        });
        findViewById(R.id.menuSupports).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, SupportActivity.class));
        });

        findViewById(R.id.menuLogOut).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            finish();
        });
    }

    private com.google.firebase.firestore.ListenerRegistration cloudListener;
    private Map<String, Boolean> activeMaintenanceMap = new java.util.HashMap<>();

    @Override
    protected void onResume() {
        super.onResume();
        refreshLabAvailability();

        // Attach Real-Time Cloud Firestore Listener for instant green/red status updates
        if (DatabaseManager.getInstance().isFirebaseAvailable()) {
            cloudListener = DatabaseManager.getInstance().listenToBookings(cloudBookings -> {
                try {
                    JSONArray cloudArray = new JSONArray();
                    for (java.util.Map<String, Object> map : cloudBookings) {
                        JSONObject obj = new JSONObject();
                        obj.put("lab", map.get("lab"));
                        obj.put("day", map.get("day"));
                        obj.put("time", map.get("time"));
                        obj.put("bookedBy", map.get("bookedBy"));
                        Number w = (Number) map.get("week_offset");
                        if (w != null) obj.put("week_offset", w.intValue());
                        cloudArray.put(obj);
                    }
                    getSharedPreferences("LabBookings", Context.MODE_PRIVATE).edit().putString("BOOKING_LIST", cloudArray.toString()).apply();
                } catch (Exception ignored) {}
                refreshLabAvailability();
            });

            DatabaseManager.getInstance().listenToLabMaintenanceStatus(map -> {
                activeMaintenanceMap = map;
                refreshLabAvailability();
            });
            DatabaseManager.getInstance().listenToFacultyAlert(this::handleFacultyAlertBroadcast);
        }
    }

    private void handleFacultyAlertBroadcast(String alertMsg) {
        androidx.cardview.widget.CardView cardAlert = findViewById(R.id.cardFacultyAlertBanner);
        TextView tvMsg = findViewById(R.id.tvFacultyAlertMsg);

        if (cardAlert != null && tvMsg != null) {
            if (alertMsg != null && !alertMsg.trim().isEmpty()) {
                tvMsg.setText(alertMsg.trim());
                cardAlert.setVisibility(android.view.View.VISIBLE);
            } else {
                cardAlert.setVisibility(android.view.View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cloudListener != null) {
            cloudListener.remove();
            cloudListener = null;
        }
    }

    private void refreshLabAvailability() {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String existingBookingsJson = prefs.getString("BOOKING_LIST", "[]");

        String currentDay = new SimpleDateFormat("EEEE", Locale.US).format(Calendar.getInstance().getTime());
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String[] labs = {"DCN", "NWL", "DNS", "SAD Lab", "Network Lab", "PGL 2", "CSD", "PGL 1", "HYP", "APL", "MML"};
        int[] statusViewIds = {R.id.statusDCN, R.id.statusNWL, R.id.statusLab1, R.id.statusSAD, R.id.statusNetwork, R.id.statusPGL2, R.id.statusLab2, R.id.statusPGL1, R.id.statusHYP, R.id.statusLab3, R.id.statusMML};

        List<ClassSession> masterList = ScheduleManager.getMasterTimetable(this);

        try {
            JSONArray bookingArray = new JSONArray(existingBookingsJson);

            int availableCount = 0;

            for (int i = 0; i < labs.length; i++) {
                String labName = labs[i];
                View statusDot = findViewById(statusViewIds[i]);
                if (statusDot == null) continue;
                
                boolean isOccupied = false;

                // 1. Check Realtime DB Maintenance status
                String normKey = labName.replaceAll("\\s+", "").toLowerCase(Locale.US);
                if (activeMaintenanceMap != null && Boolean.TRUE.equals(activeMaintenanceMap.get(normKey))) {
                    isOccupied = true;
                }

                // 2. Check User Bookings for current day & hour
                if (!isOccupied) {
                    for (int j = 0; j < bookingArray.length(); j++) {
                        JSONObject booking = bookingArray.getJSONObject(j);
                        if (booking.getString("lab").equalsIgnoreCase(labName) && booking.getString("day").equalsIgnoreCase(currentDay)) {
                            int[] range = BookingUtils.getStartEndHours(booking.getString("time"));
                            if (range != null && currentHour >= range[0] && currentHour < range[1]) {
                                isOccupied = true;
                                break;
                            }
                        }
                    }
                }

                // 3. Check Master Class Timetable for current day & hour
                if (!isOccupied) {
                    for (ClassSession cs : masterList) {
                        if (cs.getLabLocation().equalsIgnoreCase(labName) && cs.getDay().equalsIgnoreCase(currentDay)) {
                            int[] range = BookingUtils.getStartEndHours(cs.getTimeSlot());
                            if (range != null && currentHour >= range[0] && currentHour < range[1]) {
                                isOccupied = true;
                                break;
                            }
                        }
                    }
                }

                if (!isOccupied) availableCount++;
                statusDot.setBackgroundResource(isOccupied ? R.drawable.dot_red : R.drawable.dot_green);
            }

            // Update Header Banner with live stats
            String username = getIntent().getStringExtra("USER_NAME");
            if (username == null || username.isEmpty()) username = "User";
            String roleDisplay = userRole != null ? " (" + userRole + ")" : "";
            if (tvUserGreeting != null) {
                tvUserGreeting.setText("HI, " + username.toUpperCase(Locale.US) + roleDisplay + "  •  🟢 " + availableCount + "/" + labs.length + " Labs Free");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error refreshing availability", e);
        }
    }

    private void showMyBookingDialog() {
        if (getString(R.string.role_student).equals(userRole)) {
            showStudentScheduleDialog();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String existingBookingsJson = prefs.getString("BOOKING_LIST", "[]");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(getString(R.string.role_student).equals(userRole) ? R.string.dialog_my_schedule_title : R.string.dialog_my_bookings_title));

        try {
            JSONArray bookingArray = new JSONArray(existingBookingsJson);

            if (bookingArray.length() > 0) {
                StringBuilder messageBuilder = new StringBuilder(getString(R.string.msg_confirmed_reservations, bookingArray.length()));

                for (int i = 0; i < bookingArray.length(); i++) {
                    JSONObject booking = bookingArray.getJSONObject(i);
                    messageBuilder.append(i + 1).append(". ")
                            .append(getString(R.string.facility_label)).append(booking.getString("lab")).append("\n")
                            .append(getString(R.string.day_label)).append(booking.getString("day")).append("\n")
                            .append(getString(R.string.session_label)).append(booking.getString("time")).append("\n\n");
                }

                builder.setMessage(messageBuilder.toString());
                builder.setPositiveButton(getString(R.string.close), (dialog, which) -> dialog.dismiss());
                builder.setNegativeButton(getString(R.string.btn_clear_all), (dialog, which) -> cancelAllBookings());

            } else {
                builder.setMessage(getString(getString(R.string.role_student).equals(userRole) ? R.string.msg_no_schedule : R.string.msg_no_bookings));
                builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
            }

        } catch (JSONException e) {
            builder.setMessage(getString(R.string.error_loading_bookings));
            builder.setPositiveButton(getString(R.string.close), (dialog, which) -> dialog.dismiss());
        }

        builder.show();
    }

    private void cancelAllBookings() {
        getSharedPreferences("LabBookings", Context.MODE_PRIVATE).edit().clear().apply();
        Toast.makeText(this, getString(R.string.msg_reservations_cancelled), Toast.LENGTH_SHORT).show();
    }

    private AlertDialog activeStudentScheduleDialog;

    private void showStudentScheduleDialog() {
        if (activeStudentScheduleDialog != null && activeStudentScheduleDialog.isShowing()) {
            activeStudentScheduleDialog.dismiss();
            activeStudentScheduleDialog = null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_my_schedule_title);

        android.widget.ScrollView sv = new android.widget.ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 20, 32, 20);

        List<ClassSession> mySchedule = ScheduleManager.getStudentSchedule(this);

        if (mySchedule.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(R.string.msg_no_schedule);
            tvEmpty.setPadding(16, 16, 16, 16);
            container.addView(tvEmpty);
        } else {
            for (ClassSession cs : mySchedule) {
                androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                lp.setMargins(0, 0, 0, 16);
                card.setLayoutParams(lp);
                card.setRadius(12f);
                card.setCardElevation(4f);

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setPadding(24, 24, 24, 24);

                TextView tvCode = new TextView(this);
                String code = cs.getClassCode().isEmpty() ? "DFC501" : cs.getClassCode();
                tvCode.setText(code + " • " + cs.getSubjectName());
                tvCode.setTextSize(16f);
                tvCode.setTypeface(null, android.graphics.Typeface.BOLD);
                tvCode.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.campus_navy));

                TextView tvInfo = new TextView(this);
                tvInfo.setText("📅 " + cs.getDay() + " (" + cs.getTimeSlot() + ")\n📍 " + cs.getLabLocation() + " | 👨‍🏫 " + cs.getLecturerName());
                tvInfo.setTextSize(13f);
                tvInfo.setPadding(0, 8, 0, 12);

                Button btnDelete = new Button(this);
                btnDelete.setText("🗑️ Remove Class");
                btnDelete.setTextSize(12f);
                btnDelete.setAllCaps(false);
                btnDelete.setBackgroundColor(Color.parseColor("#EF4444"));
                btnDelete.setTextColor(Color.WHITE);
                final ClassSession sessionToDelete = cs;
                btnDelete.setOnClickListener(v -> {
                    ScheduleManager.removeStudentClass(this, sessionToDelete);
                    Toast.makeText(this, "Class removed from your schedule", Toast.LENGTH_SHORT).show();
                    showStudentScheduleDialog();
                });

                itemLayout.addView(tvCode);
                itemLayout.addView(tvInfo);
                itemLayout.addView(btnDelete);
                card.addView(itemLayout);
                container.addView(card);
            }
        }

        Button btnAddClass = new Button(this);
        btnAddClass.setText("➕ Add Class Manually");
        btnAddClass.setBackgroundColor(Color.parseColor("#0284C7"));
        btnAddClass.setTextColor(Color.WHITE);
        btnAddClass.setAllCaps(false);
        btnAddClass.setOnClickListener(v -> showAddStudentClassDialog());

        Button btnSampleSchedule = new Button(this);
        btnSampleSchedule.setText("⚡ Load Sample Timetable");
        btnSampleSchedule.setBackgroundColor(Color.parseColor("#6366F1"));
        btnSampleSchedule.setTextColor(Color.WHITE);
        btnSampleSchedule.setAllCaps(false);
        btnSampleSchedule.setOnClickListener(v -> loadSampleStudentSchedule());

        container.addView(btnAddClass);
        container.addView(btnSampleSchedule);
        sv.addView(container);
        builder.setView(sv);

        builder.setPositiveButton(R.string.close, (dialog, which) -> {
            dialog.dismiss();
            activeStudentScheduleDialog = null;
        });
        activeStudentScheduleDialog = builder.create();
        activeStudentScheduleDialog.show();
    }

    private void loadSampleStudentSchedule() {
        List<ClassSession> sample = new ArrayList<>();
        sample.add(new ClassSession("DFC5091", "Project 1", "Dr. Zaim", 5, "Monday", "9:00 AM - 12:00 PM", "APL"));
        sample.add(new ClassSession("DFC5082", "Network Security", "Encik Nizam", 5, "Thursday", "2:00 PM - 5:00 PM", "NWL"));
        sample.add(new ClassSession("DFC5013", "Mobile Application", "Puan Sarah", 5, "Tuesday", "8:00 AM - 11:00 AM", "PGL 1"));
        sample.add(new ClassSession("DFC5024", "Web Architecture", "Dr. Aminah", 5, "Wednesday", "11:00 AM - 1:00 PM", "CSD"));
        sample.add(new ClassSession("DFC5035", "Ethics & Cyberlaw", "Encik Farhan", 5, "Friday", "2:00 PM - 4:00 PM", "DCN"));

        ScheduleManager.saveStudentSchedule(this, sample);
        Toast.makeText(this, "Sample 5-Subject Timetable Loaded!", Toast.LENGTH_SHORT).show();
        showStudentScheduleDialog();
    }

    private void showAddStudentClassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Class to My Schedule");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
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

        final Spinner spDay = new Spinner(this);
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, 
                new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"});
        spDay.setAdapter(dayAdapter);
        layout.addView(spDay);

        List<String> times = java.util.Arrays.asList(
                "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
                "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"
        );

        final Spinner spStart = new Spinner(this);
        spStart.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, times.subList(0, times.size() - 1)));
        layout.addView(spStart);

        final Spinner spEnd = new Spinner(this);
        spEnd.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, times.subList(1, times.size())));
        layout.addView(spEnd);

        final android.widget.EditText etRoom = new android.widget.EditText(this);
        etRoom.setHint("Room / Location (e.g. Lab 1, DCN)");
        layout.addView(etRoom);

        builder.setView(layout);

        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            String code = etCode.getText().toString().trim();
            String subject = etSubject.getText().toString().trim();
            String lecturer = etLecturer.getText().toString().trim();
            String room = etRoom.getText().toString().trim();

            if (code.isEmpty()) code = "DFC501";
            if (subject.isEmpty()) subject = "Enrolled Class";
            if (lecturer.isEmpty()) lecturer = "Lecturer";
            if (room.isEmpty()) room = "Campus Lab";

            String day = spDay.getSelectedItem().toString();
            String startTime = spStart.getSelectedItem().toString();
            String endTime = spEnd.getSelectedItem().toString();
            String timeSlot = startTime + " - " + endTime;

            ClassSession session = new ClassSession(code, subject, lecturer, 5, day, timeSlot, room);
            ScheduleManager.addStudentClass(this, session);

            Toast.makeText(this, "Class added to your schedule!", Toast.LENGTH_SHORT).show();
            showStudentScheduleDialog();
        });

        builder.setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void setupLabCard(int cardId, String labName) {
        LinearLayout card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, ThirdPageActivity.class);
                intent.putExtra("LAB_NAME", labName);
                intent.putExtra("USER_ROLE", userRole);
                startActivity(intent);
            });
        }
    }
}