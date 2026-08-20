package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        ivLabPhoto = findViewById(R.id.ivLabPhoto);
        tvUserGreeting = findViewById(R.id.tvUserGreeting);

        String username = getIntent().getStringExtra("USER_NAME");
        userRole = getIntent().getStringExtra("USER_ROLE");

        if (username != null && !username.isEmpty()) {
            String roleDisplay = userRole != null ? " (" + userRole + ")" : "";
            tvUserGreeting.setText(getString(R.string.user_greeting_placeholder, username.toUpperCase() + roleDisplay));
        }

        // Open Profile when clicking the greeting
        tvUserGreeting.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(SecondPageActivity.this, ProfileActivity.class);
            intent.putExtra("USER_NAME", username);
            intent.putExtra("USER_ROLE", userRole);
            startActivity(intent);
        });

        // Change menu text for students
        if (getString(R.string.role_student).equals(userRole)) {
            TextView menuMyBooking = findViewById(R.id.menuMyBooking);
            if (menuMyBooking != null) {
                menuMyBooking.setText(R.string.menu_my_schedule);
            }
            
            // Show Retake Assistant for students
            TextView menuRetake = findViewById(R.id.menuRetakeAssistant);
            if (menuRetake != null) {
                menuRetake.setVisibility(android.view.View.VISIBLE);
                menuRetake.setOnClickListener(v -> {
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
                    startActivity(new Intent(SecondPageActivity.this, RetakeAssistantActivity.class));
                });
            }
        }

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        ivLabPhoto.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.toast_lab_photo_clicked), Toast.LENGTH_SHORT).show()
        );

        // Bind all 9 Lab Cards -> Open Booking Page (ThirdPageActivity)
        setupLabCard(R.id.cardDCN, "DCN");
        setupLabCard(R.id.cardNWL, "NWL");
        setupLabCard(R.id.cardLab1, "Lab 1");
        setupLabCard(R.id.cardPGL2, "PGL2");
        setupLabCard(R.id.cardLab2, "Lab 2");
        setupLabCard(R.id.cardPGL1, "PGL1");
        setupLabCard(R.id.cardHYP, "HYP");
        setupLabCard(R.id.cardLab3, "Lab 3");
        setupLabCard(R.id.cardMML, "MML");

        // ================= SIDE NAVIGATION DRAWER ACTIONS =================
        findViewById(R.id.menuMyBooking).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            showMyBookingDialog();
        });

        findViewById(R.id.menuLecturers).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(SecondPageActivity.this, LecturerListActivity.class));
        });

        findViewById(R.id.menuAIChatbot).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(SecondPageActivity.this, ChatbotActivity.class);
            intent.putExtra("USER_ROLE", userRole);
            startActivity(intent);
        });

        findViewById(R.id.menuAboutUs).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(SecondPageActivity.this, AboutUsActivity.class));
        });
        findViewById(R.id.menuFAQ).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(SecondPageActivity.this, FAQActivity.class));
        });
        findViewById(R.id.menuSupports).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(SecondPageActivity.this, SupportActivity.class));
        });

        findViewById(R.id.menuLogOut).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLabAvailability();
    }

    /**
     * Updates the status indicators (dots) for each lab based on current bookings and system time.
     */
    private void refreshLabAvailability() {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String existingBookingsJson = prefs.getString("BOOKING_LIST", "[]");

        // Current Day (e.g., "Monday")
        String currentDay = new SimpleDateFormat("EEEE", Locale.US).format(Calendar.getInstance().getTime());
        // Current Hour (24h format, e.g., 14)
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String[] labs = {"DCN", "NWL", "Lab 1", "PGL2", "Lab 2", "PGL1", "HYP", "Lab 3", "MML"};
        int[] statusViewIds = {R.id.statusDCN, R.id.statusNWL, R.id.statusLab1, R.id.statusPGL2, R.id.statusLab2, R.id.statusPGL1, R.id.statusHYP, R.id.statusLab3, R.id.statusMML};

        try {
            JSONArray bookingArray = new JSONArray(existingBookingsJson);

            for (int i = 0; i < labs.length; i++) {
                String labName = labs[i];
                android.view.View statusDot = findViewById(statusViewIds[i]);
                boolean isOccupied = false;

                for (int j = 0; j < bookingArray.length(); j++) {
                    JSONObject booking = bookingArray.getJSONObject(j);
                    if (booking.getString("lab").equals(labName) && booking.getString("day").equals(currentDay)) {
                        int[] range = BookingUtils.getStartEndHours(booking.getString("time"));
                        if (range != null && currentHour >= range[0] && currentHour < range[1]) {
                            isOccupied = true;
                            break;
                        }
                    }
                }

                if (isOccupied) {
                    statusDot.setBackgroundColor(getResources().getColor(R.color.status_not_available));
                } else {
                    statusDot.setBackgroundColor(getResources().getColor(R.color.status_available));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error refreshing availability", e);
        }
    }

    // ================= MULTIPLE BOOKINGS DIALOG LOGIC =================
    private void showMyBookingDialog() {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String existingBookingsJson = prefs.getString("BOOKING_LIST", "[]");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Dynamic title based on role
        if (getString(R.string.role_student).equals(userRole)) {
            builder.setTitle(getString(R.string.dialog_my_schedule_title));
        } else {
            builder.setTitle(getString(R.string.dialog_my_bookings_title));
        }

        try {
            JSONArray bookingArray = new JSONArray(existingBookingsJson);

            if (bookingArray.length() > 0) {
                StringBuilder messageBuilder = new StringBuilder(getString(R.string.msg_confirmed_reservations, bookingArray.length()));

                for (int i = 0; i < bookingArray.length(); i++) {
                    JSONObject booking = bookingArray.getJSONObject(i);
                    String lab = booking.getString("lab");
                    String day = booking.getString("day");
                    String time = booking.getString("time");

                    messageBuilder.append(i + 1).append(". ")
                            .append(getString(R.string.facility_label)).append(lab).append("\n")
                            .append(getString(R.string.day_label)).append(day).append("\n")
                            .append(getString(R.string.session_label)).append(time).append("\n\n");
                }

                builder.setMessage(messageBuilder.toString());
                builder.setPositiveButton(getString(R.string.close), (dialog, which) -> dialog.dismiss());
                builder.setNegativeButton(getString(R.string.btn_clear_all), (dialog, which) -> cancelAllBookings());

            } else {
                if (getString(R.string.role_student).equals(userRole)) {
                    builder.setMessage(getString(R.string.msg_no_schedule));
                } else {
                    builder.setMessage(getString(R.string.msg_no_bookings));
                }
                builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error loading bookings", e);
            builder.setMessage(getString(R.string.error_loading_bookings));
            builder.setPositiveButton(getString(R.string.close), (dialog, which) -> dialog.dismiss());
        }

        builder.show();
    }

    private void cancelAllBookings() {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(this, getString(R.string.msg_reservations_cancelled), Toast.LENGTH_SHORT).show();
    }

    private void setupLabCard(int cardId, String labName) {
        LinearLayout card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> {
                Intent intent = new Intent(SecondPageActivity.this, ThirdPageActivity.class);
                intent.putExtra("LAB_NAME", labName);
                intent.putExtra("USER_ROLE", userRole);
                startActivity(intent);
            });
        }
    }

    private void showToast(String item) {
        Toast.makeText(this, getString(R.string.item_selected, item), Toast.LENGTH_SHORT).show();
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}