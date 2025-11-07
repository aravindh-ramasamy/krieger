package com.example.Krieger.util;

public final class CsvEscaper {
    private CsvEscaper() {}

    /** Escape a value for CSV (normalize newlines, always quote, double internal quotes). */
    public static String escape(String s) {
        if (s == null) s = "";
        // normalize newlines to spaces to avoid breaking row structure
        String normalized = s.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
        // double any quotes
        String doubled = normalized.replace("\"", "\"\"");
        // always quote fields to be safe for commas/spaces/etc.
        return "\"" + doubled + "\"";
    }

    /** Trim to max chars and add ellipsis if truncated; also normalizes newlines. */
    public static String preview(String s, int max) {
        if (s == null) return "";
        String normalized = s.replace("\r\n", " ").replace("\n", " ").replace("\r", " ").trim();
        if (normalized.length() <= max) return normalized;
        return normalized.substring(0, Math.max(0, max - 1)) + "…";
    }
}
