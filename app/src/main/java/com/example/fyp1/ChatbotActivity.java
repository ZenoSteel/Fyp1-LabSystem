package com.example.fyp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity that provides an AI Booking Assistant interface for lab reservations.
 * Users can interact with the chatbot to check availability, view their bookings,
 * and clear their current schedule.
 */
public class ChatbotActivity extends AppCompatActivity {

    private static final String TAG = "ChatbotActivity";

    // Booking States
    private static final int STATE_IDLE = 0;
    private static final int STATE_ASK_LAB = 1;
    private static final int STATE_ASK_DAY = 2;
    private static final int STATE_ASK_TIME = 3;
    private static final int STATE_CONFIRM = 4;

    private int currentState = STATE_IDLE;
    private String pendingLab, pendingDay, pendingTime;

    /** Container for displaying chat message bubbles. */
    private LinearLayout chatContainer;
    /** ScrollView that holds the chat container for vertical scrolling. */
    private ScrollView scrollViewChat;
    /** Input field for the user to type messages. */
    private EditText etMessageInput;
    /** Button to send the user's message. */
    private ImageButton btnSend;
    /** Button to return to the previous screen. */
    private ImageButton btnBack;
    /** Quick action chip for booking a room. */
    private Button chipBookRoom;
    /** Quick action chip for checking lab schedules. */
    private Button chipCheckSchedule;
    /** Quick action chip for viewing user's bookings. */
    private Button chipMyBookings;

    /**
     * Initializes the activity, sets up UI components, and handles click events.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        chatContainer = findViewById(R.id.chatContainer);
        scrollViewChat = findViewById(R.id.scrollViewChat);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        chipBookRoom = findViewById(R.id.chipBookRoom);
        chipCheckSchedule = findViewById(R.id.chipCheckSchedule);
        chipMyBookings = findViewById(R.id.chipMyBookings);

        btnBack.setOnClickListener(v -> finish());

        addBotBubble(getString(R.string.bot_greeting));

        btnSend.setOnClickListener(v -> {
            String msg = etMessageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                addUserBubble(msg);
                etMessageInput.setText("");
                respondToUser(msg);
            }
        });

        chipBookRoom.setOnClickListener(v -> {
            addUserBubble(getString(R.string.chip_book_room));
            String userRole = getIntent().getStringExtra("USER_ROLE");
            if (getString(R.string.role_student).equals(userRole)) {
                addBotBubble("Students can only view availability. To book a lab, please use the dashboard cards or contact faculty staff.");
            } else {
                currentState = STATE_ASK_LAB;
                addBotBubble(getString(R.string.bot_msg_ask_lab));
            }
        });

        chipCheckSchedule.setOnClickListener(v -> {
            addUserBubble(getString(R.string.chip_check_schedule));
            addBotBubble(getString(R.string.bot_msg_check_schedule_prompt));
        });

        chipMyBookings.setOnClickListener(v -> {
            addUserBubble(getString(R.string.chip_my_bookings));
            checkUserBookings();
        });
    }

    /**
     * Processes the user's text query and triggers appropriate bot responses.
     * Implements basic natural language processing using keyword matching.
     *
     * @param query The message string entered by the user.
     */
    private void respondToUser(String query) {
        String lower = query.toLowerCase().trim();

        // Handle Booking Flow
        if (currentState != STATE_IDLE) {
            handleBookingFlow(lower);
            return;
        }

        // 1. Basic Greetings & Identity
        if (lower.matches(".*\\b(hi|hello|hey|greetings)\\b.*")) {
            addBotBubble(getString(R.string.bot_msg_welcome));
            return;
        }

        if (lower.contains("who are you") || lower.contains("your name")) {
            addBotBubble(getString(R.string.bot_msg_identity));
            return;
        }

        if (lower.contains("thank") || lower.contains("thanks")) {
            addBotBubble(getString(R.string.bot_msg_thanks));
            return;
        }

        // 2. Help Command
        if (lower.contains("help") || lower.equals("?")) {
            addBotBubble(getString(R.string.bot_msg_help));
            return;
        }

        // 3. Booking Management
        if (lower.contains("my booking") || lower.contains("show booking") || lower.contains("check schedule")) {
            checkUserBookings();
            return;
        }

        if (lower.contains("book") || lower.contains("reserve")) {
            String userRole = getIntent().getStringExtra("USER_ROLE");
            if (getString(R.string.role_student).equals(userRole)) {
                addBotBubble("I'm sorry, but students can only check lab availability. Please contact a lecturer to make a formal booking.");
            } else {
                currentState = STATE_ASK_LAB;
                addBotBubble(getString(R.string.bot_msg_ask_lab));
            }
            return;
        }

        if (lower.contains("cancel") || lower.contains("clear") || lower.contains("delete")) {
            clearAllBookings();
            return;
        }

        // 4. Lab Location / Info Queries
        if (handleLabInfoQuery(lower)) {
            return;
        }

        // 5. Special Activity / Whole Day Queries
        if (lower.contains("whole day") || lower.contains("all day") || lower.contains("event") || lower.contains("activity")) {
            addBotBubble(getString(R.string.bot_msg_special_event));
            return;
        }

        // 6. Availability Queries (Simulated check)
        if (handleAvailabilityQuery(lower)) {
            return;
        }

        // 6. Default Fallback
        addBotBubble(getString(R.string.bot_msg_fallback));
    }

    private void handleBookingFlow(String input) {
        switch (currentState) {
            case STATE_ASK_LAB:
                String detectedLab = detectLab(input);
                if (detectedLab != null) {
                    pendingLab = detectedLab;
                    currentState = STATE_ASK_DAY;
                    addBotBubble(getString(R.string.bot_msg_ask_day));
                } else {
                    addBotBubble(getString(R.string.bot_msg_invalid_lab));
                }
                break;

            case STATE_ASK_DAY:
                String detectedDay = detectDay(input);
                if (detectedDay != null) {
                    pendingDay = detectedDay;
                    currentState = STATE_ASK_TIME;
                    addBotBubble(getString(R.string.bot_msg_ask_time));
                } else {
                    addBotBubble(getString(R.string.bot_msg_invalid_day));
                }
                break;

            case STATE_ASK_TIME:
                // Format the user's time input to be professional
                pendingTime = BookingUtils.formatProfessionalTime(input); 
                currentState = STATE_CONFIRM;
                addBotBubble(getString(R.string.bot_msg_booking_confirm, pendingLab, pendingDay, pendingTime));
                break;

            case STATE_CONFIRM:
                if (input.contains("yes") || input.contains("confirm") || input.contains("ok")) {
                    boolean success = saveBookingDirectly(pendingLab, pendingDay, pendingTime);
                    if (success) {
                        addBotBubble(getString(R.string.bot_msg_booking_success, pendingLab));
                    }
                    currentState = STATE_IDLE;
                } else {
                    addBotBubble(getString(R.string.bot_msg_booking_cancelled));
                    currentState = STATE_IDLE;
                }
                break;
        }
    }

    private String detectLab(String input) {
        String[] labs = {"dcn", "nwl", "lab 1", "pgl2", "lab 2", "pgl1", "hyp", "lab 3", "mml"};
        for (String lab : labs) {
            if (input.contains(lab)) return lab.toUpperCase();
        }
        return null;
    }

    private String detectDay(String input) {
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday"};
        for (String day : days) {
            if (input.contains(day)) return day.substring(0, 1).toUpperCase() + day.substring(1);
        }
        return null;
    }

    private boolean saveBookingDirectly(String lab, String day, String time) {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String existingBookingsJson = prefs.getString("BOOKING_LIST", "[]");
        try {
            JSONArray bookingArray = new JSONArray(existingBookingsJson);

            // Clash Detection
            for (int i = 0; i < bookingArray.length(); i++) {
                JSONObject existing = bookingArray.getJSONObject(i);
                if (existing.getString("lab").equalsIgnoreCase(lab) &&
                    existing.getString("day").equalsIgnoreCase(day)) {
                    
                    if (BookingUtils.isOverlap(existing.getString("time"), time)) {
                        addBotBubble("Wait! I just checked and there's a clash. " + lab + " is already booked for " + day + " during that time.");
                        return false;
                    }
                }
            }

            JSONObject newBooking = new JSONObject();
            newBooking.put("lab", lab);
            newBooking.put("day", day);
            newBooking.put("time", time);
            bookingArray.put(newBooking);

            prefs.edit().putString("BOOKING_LIST", bookingArray.toString()).apply();
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "Error saving booking from bot", e);
            return false;
        }
    }

    /**
     * Handles queries about lab locations and general information.
     */
    private boolean handleLabInfoQuery(String query) {
        if (query.contains("dcn")) {
            addBotBubble(getString(R.string.bot_msg_lab_dcn));
            return true;
        } else if (query.contains("nwl")) {
            addBotBubble(getString(R.string.bot_msg_lab_nwl));
            return true;
        } else if (query.contains("pgl1") || query.contains("pgl2")) {
            addBotBubble(getString(R.string.bot_msg_lab_pgl));
            return true;
        } else if (query.contains("hyp")) {
            addBotBubble(getString(R.string.bot_msg_lab_hyp));
            return true;
        } else if (query.contains("lab 1") || query.contains("lab 2") || query.contains("lab 3")) {
            addBotBubble(getString(R.string.bot_msg_lab_general));
            return true;
        } else if (query.contains("mml")) {
            addBotBubble(getString(R.string.bot_msg_lab_mml));
            return true;
        }
        return false;
    }

    /**
     * Handles queries about lab availability at specific times.
     */
    private boolean handleAvailabilityQuery(String query) {
        String[] labs = {"dcn", "nwl", "lab 1", "pgl2", "lab 2", "pgl1", "hyp", "lab 3", "mml"};
        String detectedLab = null;

        for (String lab : labs) {
            if (query.contains(lab)) {
                detectedLab = lab.toUpperCase();
                break;
            }
        }

        if (detectedLab != null && (query.contains("free") || query.contains("available") || query.contains("vacant"))) {
            // Simulated dynamic response
            addBotBubble(getString(R.string.bot_msg_availability_sim, detectedLab));
            return true;
        }

        return false;
    }

    /**
     * Retrieves the user's saved lab bookings from SharedPreferences and displays them in the chat.
     */
    private void checkUserBookings() {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        String existingBookingsJson = prefs.getString("BOOKING_LIST", "[]");

        try {
            JSONArray bookingArray = new JSONArray(existingBookingsJson);

            if (bookingArray.length() > 0) {
                StringBuilder response = new StringBuilder(getString(R.string.bot_msg_bookings_count, bookingArray.length()));

                for (int i = 0; i < bookingArray.length(); i++) {
                    JSONObject booking = bookingArray.getJSONObject(i);
                    String lab = booking.getString("lab");
                    String day = booking.getString("day");
                    String time = booking.getString("time");

                    response.append(getString(R.string.bot_msg_booking_item, lab, day, time));
                }

                response.append(getString(R.string.bot_msg_clear_hint));
                addBotBubble(response.toString());

            } else {
                addBotBubble(getString(R.string.bot_msg_no_bookings));
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error reading bookings", e);
            addBotBubble(getString(R.string.bot_msg_error_schedule));
        }
    }

    /**
     * Clears all active lab reservations from SharedPreferences.
     */
    private void clearAllBookings() {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        addBotBubble(getString(R.string.bot_msg_clear_success));
    }

    /**
     * Adds a bot message bubble to the chat container.
     *
     * @param text The message text to be displayed.
     */
    private void addBotBubble(String text) {
        TextView bubble = new TextView(this);
        bubble.setText(getString(R.string.label_assistant) + text);
        bubble.setBackgroundColor(0xFFE2E8F0);
        bubble.setTextColor(0xFF0F172A);
        bubble.setPadding(36, 24, 36, 24);
        bubble.setTextSize(14f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 96, 8);
        params.gravity = Gravity.START;
        bubble.setLayoutParams(params);

        chatContainer.addView(bubble);
        scrollToBottom();
    }

    /**
     * Adds a user message bubble to the chat container.
     *
     * @param text The message text to be displayed.
     */
    private void addUserBubble(String text) {
        TextView bubble = new TextView(this);
        bubble.setText(getString(R.string.label_you) + text);
        bubble.setBackgroundColor(0xFF1E60AA);
        bubble.setTextColor(0xFFFFFFFF);
        bubble.setPadding(36, 24, 36, 24);
        bubble.setTextSize(14f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(96, 8, 0, 8);
        params.gravity = Gravity.END;
        bubble.setLayoutParams(params);

        chatContainer.addView(bubble);
        scrollToBottom();
    }

    /**
     * Automatically scrolls the chat ScrollView to the bottom to show new messages.
     */
    private void scrollToBottom() {
        scrollViewChat.post(() -> scrollViewChat.fullScroll(ScrollView.FOCUS_DOWN));
    }
}