package com.example.Krieger.util;

public final class TextMetrics {

    private TextMetrics() {}

    /** Normalize CRLF/CR to LF. */
    public static String normalizeNewlines(String s) {
        if (s == null) return "";
        return s.replace("\r\n", "\n").replace("\r", "\n");
    }

    /** Count words after trimming; split on Unicode whitespace. */
    public static int countWords(String s) {
        if (s == null) return 0;
        String t = s.trim();
        if (t.isEmpty()) return 0;
        String[] parts = t.split("\\s+");
        int c = 0;
        for (String p : parts) {
            if (!p.isEmpty()) c++;
        }
        return c;
    }

    /** Count characters (including spaces and newlines) after normalization. */
    public static int countChars(String normalized) {
        if (normalized == null) return 0;
        return normalized.length();
    }

    /** Count lines by LF after normalization. Returns 0 for empty text, else (#LF + 1). */
    public static int countLines(String normalized) {
        if (normalized == null || normalized.isBlank()) return 0;
        int lines = 1;
        for (int i = 0; i < normalized.length(); i++) {
            if (normalized.charAt(i) == '\n') lines++;
        }
        return lines;
    }

    /** Clamp WPM to [100..400], use default if null/invalid. */
    public static int clampWpm(Integer wpmParam, int defaultWpm) {
        int wpm = (wpmParam == null) ? defaultWpm : wpmParam.intValue();
        if (wpm < 100) wpm = 100;
        if (wpm > 400) wpm = 400;
        return wpm;
    }

    /** ceil(words / wpm * 60); zero words => 0 seconds. */
    public static int estimateReadingTimeSeconds(int words, int wpm) {
        if (words <= 0) return 0;
        double seconds = ((double) words / (double) wpm) * 60.0;
        return (int) Math.ceil(seconds);
    }
}
