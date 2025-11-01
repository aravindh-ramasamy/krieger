package com.example.Krieger.events;

public final class EventCodec {
    private EventCodec() {}

    // Encodes as: "DELETE: <id>"
    public static String encode(EventType type, long id) {
        return switch (type) {
            case DELETE -> "DELETE: " + id;
        };
    }
}
