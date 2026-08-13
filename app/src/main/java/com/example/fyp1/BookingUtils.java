package com.example.fyp1;

/**
 * Utility class to handle time parsing and overlap detection for lab bookings.
 */
public class BookingUtils {

    /**
     * Checks if two time slots overlap.
     * @param slot1 First time slot string (e.g., "8:00 AM - 11:00 AM")
     * @param slot2 Second time slot string (e.g., "10am - 12pm")
     * @return true if they overlap, false otherwise.
     */
    public static boolean isOverlap(String slot1, String slot2) {
        int[] hours1 = getStartEndHours(slot1);
        int[] hours2 = getStartEndHours(slot2);

        if (hours1 == null || hours2 == null) return false;

        // Mathematical overlap logic: (StartA < EndB) AND (StartB < EndA)
        return hours1[0] < hours2[1] && hours2[0] < hours1[1];
    }

    /**
     * Parses a time slot string into start and end hours (24-hour format).
     */
    public static int[] getStartEndHours(String slot) {
        if (slot == null) return null;
        String lower = slot.toLowerCase();
        
        // Standard Lab Full Day
        if (lower.contains("full day")) return new int[]{9, 17};

        try {
            String[] parts = lower.split("-");
            if (parts.length < 2) return null;

            int start = parseTimeHour(parts[0].trim());
            int end = parseTimeHour(parts[1].split("\\(")[0].trim());
            
            if (start == -1 || end == -1) return null;
            return new int[]{start, end};
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Formats a raw time input into a professional string.
     * e.g., "11am - 2pm" -> "11:00 AM - 2:00 PM (3 hrs)"
     */
    public static String formatProfessionalTime(String rawInput) {
        int[] hours = getStartEndHours(rawInput);
        if (hours == null) return rawInput; // Fallback to raw if unparseable

        int start = hours[0];
        int end = hours[1];
        int duration = end - start;

        return formatHour(start) + " - " + formatHour(end) + " (" + duration + " hrs)";
    }

    private static String formatHour(int hour24) {
        if (hour24 == 0) return "12:00 AM";
        if (hour24 == 12) return "12:00 PM";
        if (hour24 > 12) return (hour24 - 12) + ":00 PM";
        return hour24 + ":00 AM";
    }

    /**
     * Extracts the hour from a time string and converts to 24h format.
     * Handles formats like "8:00 AM", "8am", "2 PM", etc.
     */
    private static int parseTimeHour(String time) {
        try {
            int hour;
            if (time.contains(":")) {
                hour = Integer.parseInt(time.split(":")[0].trim());
            } else {
                // Remove all non-digits to get the hour (e.g., "8am" -> "8")
                String digits = time.replaceAll("[^0-9]", "");
                if (digits.isEmpty()) return -1;
                hour = Integer.parseInt(digits);
            }

            // Adjust for AM/PM
            if (time.contains("pm") && hour != 12) hour += 12;
            if (time.contains("am") && hour == 12) hour = 0;

            return hour;
        } catch (Exception e) {
            return -1;
        }
    }
}