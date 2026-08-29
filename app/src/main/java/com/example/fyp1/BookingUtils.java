package com.example.fyp1;

/**
 * Utility class to handle time parsing and overlap detection for lab bookings.
 * Enhanced to handle natural language inputs like "8 to 10".
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
     * Supports "-", "to", "until", and "–".
     */
    public static int[] getStartEndHours(String slot) {
        if (slot == null || slot.isEmpty() || slot.equalsIgnoreCase("unknown")) {
            return new int[]{0, 0};
        }
        
        String lower = slot.toLowerCase().replace("am", " am").replace("pm", " pm").trim();
        if (lower.contains("full day")) return new int[]{9, 17};

        try {
            // Support multiple separators
            String separator = "-";
            if (lower.contains(" to ")) separator = " to ";
            else if (lower.contains(" until ")) separator = " until ";
            else if (lower.contains("–")) separator = "–";

            String[] parts = lower.split(separator);
            if (parts.length < 2) return new int[]{0, 0};

            int start = parseTimeHour(parts[0].trim());
            int end = parseTimeHour(parts[1].split("\\(")[0].trim());
            
            if (start == -1 || end == -1) return new int[]{0, 0};
            
            return new int[]{start, end};
        } catch (Exception e) {
            return new int[]{0, 0};
        }
    }

    /**
     * Returns exactly "8:00 AM - 10:00 AM" format.
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

            // Simple logic: assume 8-11 is AM, 12-5 is PM if not specified
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