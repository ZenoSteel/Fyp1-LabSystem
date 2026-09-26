package com.example.fyp1;

import java.util.HashMap;
import java.util.Map;

/**
 * The "Brain" of the chatbot. Handles fuzzy logic, synonym mapping, 
 * and intent detection for all 11 campus labs.
 */
public class BotBrain {

    private static final Map<String, String[]> LAB_SYNONYMS = new HashMap<>();

    static {
        LAB_SYNONYMS.put("DCN", new String[]{"dcn", "data communication", "networking lab", "dc-n"});
        LAB_SYNONYMS.put("NWL", new String[]{"nwl", "wireless lab", "wireless"});
        LAB_SYNONYMS.put("DNS", new String[]{"dns", "lab 1", "lab1", "general lab 1"});
        LAB_SYNONYMS.put("SAD Lab", new String[]{"sad lab", "sad", "system analysis"});
        LAB_SYNONYMS.put("Network Lab", new String[]{"network lab", "network"});
        LAB_SYNONYMS.put("PGL 1", new String[]{"pgl1", "programming lab 1", "pgl 1", "coding 1"});
        LAB_SYNONYMS.put("PGL 2", new String[]{"pgl2", "programming lab 2", "pgl 2", "coding 2"});
        LAB_SYNONYMS.put("CSD", new String[]{"csd", "lab 2", "lab2", "general lab 2"});
        LAB_SYNONYMS.put("HYP", new String[]{"hyp", "hyper", "high performance", "hypermedia"});
        LAB_SYNONYMS.put("APL", new String[]{"apl", "lab 3", "lab3", "general lab 3"});
        LAB_SYNONYMS.put("MML", new String[]{"mml", "multimedia", "media lab", "design lab"});
    }

    /**
     * Detects a lab name from raw user input using synonym matching and fuzzy logic.
     */
    public static String detectLabSmart(String input) {
        if (input == null) return null;
        String lowerInput = input.toLowerCase();

        // 1. Direct Synonym Match
        for (Map.Entry<String, String[]> entry : LAB_SYNONYMS.entrySet()) {
            for (String synonym : entry.getValue()) {
                if (lowerInput.contains(synonym)) {
                    return entry.getKey();
                }
            }
        }

        // 2. Fuzzy Match (handle small typos)
        String[] words = lowerInput.split("\\s+");
        for (String word : words) {
            if (word.length() < 3) continue;
            for (String labCode : LAB_SYNONYMS.keySet()) {
                if (calculateSimilarity(word, labCode.toLowerCase()) > 0.7) {
                    return labCode;
                }
            }
        }

        return null;
    }

    /**
     * Detects if the user's query is asking for information vs wanting to book.
     */
    public static boolean isInformationalQuery(String input) {
        String lower = input.toLowerCase();
        return lower.contains("what is") || lower.contains("tell me about") || 
               lower.contains("info") || lower.contains("where");
    }

    /**
     * Simple Jaro-Winkler or Levenshtein-like similarity check.
     * Returns a score between 0 and 1.
     */
    private static double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int longerLength = Math.max(s1.length(), s2.length());
        if (longerLength == 0) return 1.0;
        return (longerLength - editDistance(s1, s2)) / (double) longerLength;
    }

    private static int editDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}
