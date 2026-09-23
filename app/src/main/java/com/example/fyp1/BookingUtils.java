package com.example.fyp1;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to handle time parsing and overlap detection for lab bookings.
 * Enhanced to handle natural language inputs like "1pm 4pm", "8 to 10", or "1pm - 4pm".
 */
public class BookingUtils {

    public static boolean isOverlap(String slot1, String slot2) {
        int[] hours1 = getStartEndHours(slot1);
        int[] hours2 = getStartEndHours(slot2);
        if (hours1[0] == 0 && hours1[1] == 0) return false;
        if (hours2[0] == 0 && hours2[1] == 0) return false;
        return hours1[0] < hours2[1] && hours2[0] < hours1[1];
    }

    /**
     * Enhanced parser for start and end hours.
     * Supports "1pm 4pm", "1pm - 4pm", "8 to 10", "until", and "–".
     */
    public static int[] getStartEndHours(String slot) {
        if (slot == null || slot.isEmpty() || slot.equalsIgnoreCase("unknown")) {
            return new int[]{0, 0};
        }
        
        String lower = slot.toLowerCase().replace("am", " am").replace("pm", " pm").trim();
        if (lower.contains("full day")) return new int[]{9, 17};

        try {
            // Regex to find explicit time tokens (e.g. 1 pm, 4 pm, 08:00 am, 10 am, 2pm)
            Matcher matcher = Pattern.compile("(?i)\\b(1[0-2]|0?[1-9])(:[0-5][0-9])?\\s*(am|pm)?\\b").matcher(lower);

            List<String> matches = new ArrayList<>();
            while (matcher.find()) {
                matches.add(matcher.group().trim());
            }

            if (matches.size() >= 2) {
                int start = parseTimeHour(matches.get(0));
                int end = parseTimeHour(matches.get(1));
                if (start != -1 && end != -1 && end > start) {
                    return new int[]{start, end};
                }
            }

            // Fallback separator logic
            String separator = "-";
            if (lower.contains(" to ")) separator = " to ";
            else if (lower.contains(" until ")) separator = " until ";
            else if (lower.contains("–")) separator = "–";

            String[] parts = lower.split(separator);
            if (parts.length >= 2) {
                int start = parseTimeHour(parts[0].trim());
                int end = parseTimeHour(parts[1].split("\\(")[0].trim());
                if (start != -1 && end != -1) {
                    return new int[]{start, end};
                }
            }
        } catch (Exception ignored) {}

        return new int[]{0, 0};
    }

    /**
     * Returns formatted time string e.g. "01:00 PM - 04:00 PM".
     */
    public static String formatProfessionalTime(String rawInput) {
        int[] hours = getStartEndHours(rawInput);
        if (hours[0] == 0 && hours[1] == 0) return rawInput;

        return formatHour(hours[0]) + " - " + formatHour(hours[1]);
    }

    private static String formatHour(int hour24) {
        if (hour24 == 0) return "12:00 AM";
        if (hour24 == 12) return "12:00 PM";
        if (hour24 > 12) return (hour24 - 12) + ":00 PM";
        return hour24 + ":00 AM";
    }

    private static int parseTimeHour(String time) {
        try {
            int hour;
            if (time.contains(":")) {
                hour = Integer.parseInt(time.split(":")[0].trim());
            } else {
                String digits = time.replaceAll("[^0-9]", "");
                if (digits.isEmpty()) return -1;
                hour = Integer.parseInt(digits);
            }

            // AM/PM handling
            if (time.contains("pm") && hour != 12) hour += 12;
            else if (time.contains("am") && hour == 12) hour = 0;
            else if (!time.contains("am") && !time.contains("pm")) {
                if (hour < 8) hour += 12; // Assume 1-7 is PM
            }

            return hour;
        } catch (Exception e) {
            return -1;
        }
    }
}
