package com.example.Krieger.util;

public final class TextSanitizer {
    private TextSanitizer() {}

    /** Strip simple HTML tags; not an HTML parser, just lightweight cleanup for previews. */
    public static String stripHtml(String in) {
        if (in == null || in.isEmpty()) return in;
        // remove tags
        String out = in.replaceAll("<[^>]+>", " ");
        // collapse whitespace
        out = out.replaceAll("\\s+", " ").trim();
        return out;
    }
}
