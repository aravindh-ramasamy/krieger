package com.example.Krieger.util;

import java.text.Normalizer;

public final class CsvEscaper {
    private CsvEscaper() {}

    /** Escape a value for CSV (normalize newlines, always quote, double internal quotes). */
    public static String escape(String s) {
        if (s == null) s = "";
        String normalized = s.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
        String doubled = normalized.replace("\"", "\"\"");
        return "\"" + doubled + "\"";
    }

    /** Trim to max chars and add ellipsis if truncated; also normalizes newlines. */
    public static String preview(String s, int max) {
        if (s == null) return "";
        String normalized = s.replace("\r\n", " ").replace("\n", " ").replace("\r", " ").trim();
        if (normalized.length() <= max) return normalized;
        return normalized.substring(0, Math.max(0, max - 1)) + "…";
    }

    /** Resolve delimiter from request param; defaults to comma. Accepts: comma, semicolon, tab. */
    public static char resolveDelimiter(String param) {
        if (param == null || param.isBlank()) return ',';
        switch (param.trim().toLowerCase()) {
            case "semicolon":
            case "semi":
            case "sc":
                return ';';
            case "tab":
            case "tsv":
                return '\t';
            case "comma":
            case "csv":
            default:
                return ',';
        }
    }

    /** Clamp an integer between min and max, using defaultVal when val is null. */
    public static int clamp(Integer val, int min, int max, int defaultVal) {
        int v = (val == null) ? defaultVal : val.intValue();
        if (v < min) v = min;
        if (v > max) v = max;
        return v;
    }

    /** Append a CSV row to the builder using the given delimiter. */
    public static void appendRow(StringBuilder sb, char delimiter, String... values) {
        if (values == null || values.length == 0) {
            sb.append('\n');
            return;
        }
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(escape(values[i]));
        }
        sb.append('\n');
    }

    /**
     * Sanitize a suggested filename (no path separators/control chars). Returns fallback if empty.
     * Does not append extension; caller decides.
     */
    public static String sanitizeFilename(String input, String fallback) {
        String name = (input == null) ? "" : input.trim();
        if (name.isEmpty()) return fallback;
        // strip dangerous chars and normalize
        name = Normalizer.normalize(name, Normalizer.Form.NFKC)
                .replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_")
                .replaceAll("\\s+", "_");
        // guard overly long names
        if (name.length() > 120) name = name.substring(0, 120);
        if (name.isEmpty() || name.equals(".") || name.equals("..")) return fallback;
        return name;
    }
}
