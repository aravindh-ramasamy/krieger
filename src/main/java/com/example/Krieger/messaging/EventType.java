package com.example.Krieger.messaging;

public enum EventType {
    CREATE, UPDATE, DELETE;

    public static EventType fromString(String s) {
        if (s == null) return null;
        try { return EventType.valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }
}