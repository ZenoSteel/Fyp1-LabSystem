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
        setupLabCard(R.id.cardLab1, "Lab 1");
        
        // First Floor
        setupLabCard(R.id.cardSAD, "SAD Lab");
        setupLabCard(R.id.cardNetwork, "Network Lab");
        
        // Second Floor
        setupLabCard(R.id.cardPGL2, "PGL2");
        setupLabCard(R.id.cardLab2, "Lab 2");
        setupLabCard(R.id.cardPGL1, "PGL1");
        
        // Third Floor
        setupLabCard(R.id.cardHYP, "HYP");
        setupLabCard(R.id.cardLab3, "Lab 3");
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

    @Override
    protected void onResume() {
        super.onResume();
        refreshLabAvailability();
    }

    private void refreshLabAvailability() {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String existingBookingsJson = prefs.getString("BOOKING_LIST", "[]");

        String currentDay = new SimpleDateFormat("EEEE", Locale.US).format(Calendar.getInstance().getTime());
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String[] labs = {"DCN", "NWL", "Lab 1", "SAD Lab", "Network Lab", "PGL2", "Lab 2", "PGL1", "HYP", "Lab 3", "MML"};
        int[] statusViewIds = {R.id.statusDCN, R.id.statusNWL, R.id.statusLab1, R.id.statusSAD, R.id.statusNetwork, R.id.statusPGL2, R.id.statusLab2, R.id.statusPGL1, R.id.statusHYP, R.id.statusLab3, R.id.statusMML};

        try {
            JSONArray bookingArray = new JSONArray(existingBookingsJson);

            for (int i = 0; i < labs.length; i++) {
                String labName = labs[i];
                android.view.View statusDot = findViewById(statusViewIds[i]);
                if (statusDot == null) continue;
                
                boolean isOccupied = false;

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

                statusDot.setBackgroundResource(isOccupied ? R.drawable.dot_red : R.drawable.dot_green);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error refreshing availability", e);
        }
    }

    private void showMyBookingDialog() {
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