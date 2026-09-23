package com.example.fyp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Activity that provides a Persistent AI Booking Assistant interface.
 * Uses Regex for precise intent detection and supports contextual memory.
 * Restricted to weekdays only for booking and checks.
 */
public class ChatbotActivity extends AppCompatActivity {

    private static final String TAG = "ChatbotActivity";
    private static final String PREF_CHAT_HISTORY = "chat_history";

    // Booking States
    private static final int STATE_IDLE = 0;
    private static final int STATE_ASK_LAB = 1;
    private static final int STATE_ASK_DAY = 2;
    private static final int STATE_ASK_TIME = 3;
    private static final int STATE_CONFIRM = 4;
    private static final int STATE_PROPOSE_BOOKING = 5;

    private int currentState = STATE_IDLE;
    private String pendingLab, pendingDay, pendingTime;
    private String lastMentionedLab = null;

    private LinearLayout chatContainer;
    private ScrollView scrollViewChat;
    private EditText etMessageInput;
    private ImageButton btnSend, btnBack;
    private Button chipBookRoom, chipCheckSchedule, chipMyBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        initializeViews();
        setupClickListeners();

        if (!loadChatHistory()) {
            addBotBubble(getString(R.string.bot_greeting));
        }
    }

    private void initializeViews() {
        chatContainer = findViewById(R.id.chatContainer);
        scrollViewChat = findViewById(R.id.scrollViewChat);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        chipBookRoom = findViewById(R.id.chipBookRoom);
        chipCheckSchedule = findViewById(R.id.chipCheckSchedule);
        chipMyBookings = findViewById(R.id.chipMyBookings);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
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
            startBookingFlow("");
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

    private void startBookingFlow(String initialQuery) {
        String userRole = getIntent().getStringExtra("USER_ROLE");
        if (getString(R.string.role_student).equals(userRole)) {
            addBotBubble("Students can only view availability. To book a lab, please use the dashboard cards or contact faculty staff.");
        } else {
            String lab = detectLab(initialQuery);
            String day = detectDay(initialQuery);
            String time = detectTime(initialQuery);
            
            if (isWeekend(day)) {
                addBotBubble("I'm sorry, but labs are closed on " + day + ". Please select a weekday (Monday to Friday) for your booking.");
                return;
            }

            if (lab != null && day != null && time != null) {
                pendingLab = lab;
                pendingDay = day;
                pendingTime = time;
                currentState = STATE_CONFIRM;
                addBotBubble(getString(R.string.bot_msg_booking_confirm, pendingLab, pendingDay, pendingTime));
            } else if (lab != null && day != null) {
                pendingLab = lab;
                pendingDay = day;
                currentState = STATE_ASK_TIME;
                addBotBubble("Excellent! I've noted " + lab + " for " + day + ". What time slot should I record? (e.g. 8 to 10)");
            } else if (lab != null) {
                pendingLab = lab;
                currentState = STATE_ASK_DAY;
                addBotBubble("Sure! I've noted " + lab + ". Which day would you like to book? (Monday - Friday)");
            } else {
                currentState = STATE_ASK_LAB;
                addBotBubble(getString(R.string.bot_msg_ask_lab));
            }
        }
    }

    private void respondToUser(String query) {
        String lower = query.toLowerCase().trim();

        // 1. GLOBAL COMMANDS (Always work)
        boolean isClear = Pattern.compile("\\b(clear|delete|wipe|cancel)\\b").matcher(lower).find();
        if (isClear) {
            clearAllBookings();
            clearChatHistory();
            currentState = STATE_IDLE; // Reset state just in case
            return;
        }

        // 2. STATE MACHINE (If we are in a middle of a flow)
        if (currentState != STATE_IDLE) {
            handleBookingFlow(lower);
            return;
        }

        // 3. REGULAR INTENTS
        if (handleAdvancedQuery(lower)) {
            return;
        }

        boolean isGreeting = Pattern.compile("\\b(hi|hello|hey|yo|morning)\\b").matcher(lower).find();
        if (isGreeting) {
            addBotBubble(getString(R.string.bot_msg_welcome));
            return;
        }

        boolean isBook = Pattern.compile("\\b(book|reserve|make)\\b").matcher(lower).find();
        if (isBook && !lower.contains("booking")) {
             startBookingFlow(lower);
             return;
        }

        if (lower.contains("my booking") || lower.contains("my schedule")) {
            checkUserBookings();
            return;
        }

        if (handleLabInfoQuery(lower)) {
            return;
        }

        if (lower.contains("help") || lower.equals("?")) {
            addBotBubble(getString(R.string.bot_msg_help));
        } else {
            addBotBubble(getString(R.string.bot_msg_fallback));
        }
    }

    private void handleBookingFlow(String input) {
        switch (currentState) {
            case STATE_PROPOSE_BOOKING: {
                if (input.contains("yes") || input.contains("ok") || input.contains("sure") || input.contains("want")) {
                    currentState = STATE_ASK_TIME;
                    addBotBubble(getString(R.string.bot_msg_ask_time));
                } else {
                    addBotBubble("Understood. Let me know if you need anything else!");
                    currentState = STATE_IDLE;
                }
                break;
            }

            case STATE_ASK_LAB: {
                String lab = detectLab(input);
                String day = detectDay(input);
                String time = detectTime(input);

                if (lab != null && day != null && time != null) {
                    if (isWeekend(day)) {
                        addBotBubble("The labs are not open on weekends. Please choose a weekday (Monday to Friday).");
                        return;
                    }
                    pendingLab = lab;
                    pendingDay = day;
                    pendingTime = time;
                    currentState = STATE_CONFIRM;
                    addBotBubble(getString(R.string.bot_msg_booking_confirm, pendingLab, pendingDay, pendingTime));
                } else if (lab != null && day != null) {
                    if (isWeekend(day)) {
                        addBotBubble("The labs are not open on weekends. Please choose a weekday (Monday to Friday).");
                        return;
                    }
                    pendingLab = lab;
                    pendingDay = day;
                    currentState = STATE_ASK_TIME;
                    addBotBubble(getString(R.string.bot_msg_ask_time));
                } else if (lab != null) {
                    pendingLab = lab;
                    currentState = STATE_ASK_DAY;
                    addBotBubble(getString(R.string.bot_msg_ask_day));
                } else {
                    addBotBubble(getString(R.string.bot_msg_invalid_lab));
                }
                break;
            }

            case STATE_ASK_DAY: {
                String day = detectDay(input);
                if (day != null) {
                    if (isWeekend(day)) {
                        addBotBubble("The labs are not open on weekends. Please choose a weekday (Monday to Friday).");
                        return;
                    }
                    pendingDay = day;
                    currentState = STATE_ASK_TIME;
                    addBotBubble(getString(R.string.bot_msg_ask_time));
                } else addBotBubble(getString(R.string.bot_msg_invalid_day));
                break;
            }

            case STATE_ASK_TIME:
                pendingTime = BookingUtils.formatProfessionalTime(input);
                currentState = STATE_CONFIRM;
                addBotBubble(getString(R.string.bot_msg_booking_confirm, pendingLab, pendingDay, pendingTime));
                break;

            case STATE_CONFIRM:
                if (input.contains("yes") || input.contains("confirm") || input.contains("ok")) {
                    if (saveBookingDirectly(pendingLab, pendingDay, pendingTime)) {
                        addBotBubble(getString(R.string.bot_msg_booking_success, pendingLab));
                    }
                } else addBotBubble(getString(R.string.bot_msg_booking_cancelled));
                currentState = STATE_IDLE;
                break;
        }
    }

    private boolean handleAdvancedQuery(String query) {
        boolean isAskingFree = Pattern.compile("\\b(free|available|vacant|schedule|busy)\\b").matcher(query).find();
        if (!isAskingFree) return false;

        String lab = detectLab(query);
        if (lab == null) lab = lastMentionedLab;
        
        String day = detectDay(query);
        if (day == null) day = detectDay("today");

        if (lab != null) {
            if (isWeekend(day)) {
                addBotBubble("The labs are closed on " + day + ". Feel free to check any weekday instead!");
                return true;
            }
            checkRealTimeAvailability(lab, day);
            return true;
        }
        return false;
    }

    private void checkRealTimeAvailability(String lab, String day) {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        try {
            JSONArray bookings = new JSONArray(prefs.getString("BOOKING_LIST", "[]"));
            List<String> occupied = new ArrayList<>();
            for (int i = 0; i < bookings.length(); i++) {
                JSONObject b = bookings.getJSONObject(i);
                if (b.getString("lab").equalsIgnoreCase(lab) && b.getString("day").equalsIgnoreCase(day)) {
                    occupied.add(b.getString("time"));
                }
            }

            if (occupied.isEmpty()) {
                addBotBubble("Analyzing... 🤖\n" + lab + " is completely free on " + day + "! Would you like me to start a booking for you?");
                pendingLab = lab;
                pendingDay = day;
                currentState = STATE_PROPOSE_BOOKING;
            } else {
                StringBuilder sb = new StringBuilder("On " + day + ", " + lab + " is busy during:\n");
                for (String s : occupied) sb.append("• ").append(s).append("\n");
                sb.append("\nYou can book any other time today.");
                pendingLab = lab;
                pendingDay = day;
                currentState = STATE_PROPOSE_BOOKING;
                addBotBubble(sb.toString());
            }
        } catch (Exception e) {
            addBotBubble("I'm sorry, I had trouble reading the database. Try again?");
        }
    }

    private String detectLab(String input) {
        String lab = BotBrain.detectLabSmart(input);
        if (lab != null) {
            lastMentionedLab = lab;
        }
        return lab;
    }

    private String detectDay(String input) {
        String day = null;
        if (input.contains("today")) {
            day = new java.text.SimpleDateFormat("EEEE", Locale.US).format(java.util.Calendar.getInstance().getTime());
        } else if (input.contains("tomorrow")) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.add(java.util.Calendar.DATE, 1);
            day = new java.text.SimpleDateFormat("EEEE", Locale.US).format(c.getTime());
        } else {
            String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
            for (String d : days) {
                if (input.contains(d)) {
                    day = d.substring(0, 1).toUpperCase() + d.substring(1);
                    break;
                }
            }
        }
        return day;
    }

    private String detectTime(String input) {
        if (input == null || input.isEmpty()) return null;
        int[] hours = BookingUtils.getStartEndHours(input);
        if (hours != null && hours[0] > 0 && hours[1] > hours[0]) {
            return BookingUtils.formatProfessionalTime(input);
        }
        return null;
    }

    private boolean isWeekend(String day) {
        return day != null && (day.equalsIgnoreCase("Saturday") || day.equalsIgnoreCase("Sunday"));
    }

    private boolean saveBookingDirectly(String lab, String day, String time) {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        try {
            JSONArray bookings = new JSONArray(prefs.getString("BOOKING_LIST", "[]"));
            JSONObject nb = new JSONObject();
            nb.put("lab", lab);
            nb.put("day", day);
            nb.put("time", time);
            bookings.put(nb);
            prefs.edit().putString("BOOKING_LIST", bookings.toString()).apply();
            return true;
        } catch (Exception e) { return false; }
    }

    private boolean handleLabInfoQuery(String query) {
        String lab = BotBrain.detectLabSmart(query);
        if (lab == null) return false;

        if (BotBrain.isInformationalQuery(query)) {
            switch (lab) {
                case "DCN": addBotBubble(getString(R.string.bot_msg_lab_dcn)); break;
                case "NWL": addBotBubble(getString(R.string.bot_msg_lab_nwl)); break;
                case "MML": addBotBubble(getString(R.string.bot_msg_lab_mml)); break;
                case "HYP": addBotBubble(getString(R.string.bot_msg_lab_hyp)); break;
                default: addBotBubble(getString(R.string.bot_msg_lab_general)); break;
            }
            return true;
        }
        return false;
    }

    private void checkUserBookings() {
        SharedPreferences prefs = getSharedPreferences("LabBookings", Context.MODE_PRIVATE);
        try {
            JSONArray bookings = new JSONArray(prefs.getString("BOOKING_LIST", "[]"));
            if (bookings.length() > 0) {
                StringBuilder sb = new StringBuilder(getString(R.string.bot_msg_bookings_count, bookings.length()));
                for (int i = 0; i < bookings.length(); i++) {
                    JSONObject b = bookings.getJSONObject(i);
                    sb.append(getString(R.string.bot_msg_booking_item, b.getString("lab"), b.getString("day"), b.getString("time")));
                }
                sb.append(getString(R.string.bot_msg_clear_hint));
                addBotBubble(sb.toString());
            } else addBotBubble(getString(R.string.bot_msg_no_bookings));
        } catch (Exception e) { addBotBubble(getString(R.string.bot_msg_error_schedule)); }
    }

    private void clearAllBookings() {
        getSharedPreferences("LabBookings", Context.MODE_PRIVATE).edit().clear().apply();
        addBotBubble(getString(R.string.bot_msg_clear_success));
    }

    private String getChatHistoryKey() {
        String username = getIntent().getStringExtra("USER_NAME");
        String role = getIntent().getStringExtra("USER_ROLE");
        if (username != null && !username.trim().isEmpty()) {
            return "chat_history_" + username.trim().toLowerCase(Locale.US);
        } else if (role != null && !role.trim().isEmpty()) {
            return "chat_history_" + role.trim().toLowerCase(Locale.US);
        }
        return PREF_CHAT_HISTORY;
    }

    private void saveChatMessage(String sender, String text) {
        SharedPreferences prefs = getSharedPreferences("ChatHistory", Context.MODE_PRIVATE);
        try {
            String key = getChatHistoryKey();
            JSONArray history = new JSONArray(prefs.getString(key, "[]"));
            JSONObject m = new JSONObject();
            m.put("sender", sender);
            m.put("text", text);
            history.put(m);
            prefs.edit().putString(key, history.toString()).apply();
        } catch (Exception ignored) {}
    }

    private boolean loadChatHistory() {
        SharedPreferences prefs = getSharedPreferences("ChatHistory", Context.MODE_PRIVATE);
        try {
            String key = getChatHistoryKey();
            JSONArray history = new JSONArray(prefs.getString(key, "[]"));
            if (history.length() == 0) return false;
            for (int i = 0; i < history.length(); i++) {
                JSONObject m = history.getJSONObject(i);
                if (m.getString("sender").equals("bot")) renderBotBubble(m.getString("text"));
                else renderUserBubble(m.getString("text"));
            }
            return true;
        } catch (Exception e) { return false; }
    }

    private void clearChatHistory() {
        String key = getChatHistoryKey();
        getSharedPreferences("ChatHistory", Context.MODE_PRIVATE).edit().remove(key).apply();
    }

    private void addBotBubble(String text) {
        renderBotBubble(text);
        saveChatMessage("bot", text);
    }

    private void renderBotBubble(String text) {
        TextView b = new TextView(this);
        b.setText(getString(R.string.label_assistant) + text);
        b.setBackgroundResource(R.drawable.bg_chat_bubble_bot);
        b.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_primary));

        int pLR = (int) (16 * getResources().getDisplayMetrics().density);
        int pTB = (int) (12 * getResources().getDisplayMetrics().density);
        b.setPadding(pLR, pTB, pLR, pTB);
        b.setTextSize(14f);
        b.setLineSpacing(0, 1.2f);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
        int marginSides = (int) (48 * getResources().getDisplayMetrics().density);
        int marginVertical = (int) (8 * getResources().getDisplayMetrics().density);
        p.setMargins(0, marginVertical, marginSides, marginVertical);
        p.gravity = Gravity.START;
        b.setLayoutParams(p);
        chatContainer.addView(b);
        scrollToBottom();
    }

    private void addUserBubble(String text) {
        renderUserBubble(text);
        saveChatMessage("user", text);
    }

    private void renderUserBubble(String text) {
        TextView b = new TextView(this);
        b.setText(getString(R.string.label_you) + text);
        b.setBackgroundResource(R.drawable.bg_chat_bubble_user);
        b.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_light));

        int pLR = (int) (16 * getResources().getDisplayMetrics().density);
        int pTB = (int) (12 * getResources().getDisplayMetrics().density);
        b.setPadding(pLR, pTB, pLR, pTB);
        b.setTextSize(14f);
        b.setLineSpacing(0, 1.2f);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int marginSides = (int) (48 * getResources().getDisplayMetrics().density);
        int marginVertical = (int) (8 * getResources().getDisplayMetrics().density);
        p.setMargins(marginSides, marginVertical, 0, marginVertical);
        p.gravity = Gravity.END;
        b.setLayoutParams(p);
        chatContainer.addView(b);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollViewChat.post(() -> scrollViewChat.fullScroll(ScrollView.FOCUS_DOWN));
    }
}