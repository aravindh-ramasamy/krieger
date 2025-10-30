package com.example.Krieger.messaging;

import java.util.Optional;
import java.util.regex.Pattern;

public final class EventCodec {
    private static final Pattern PATTERN = Pattern.compile("^([A-Z]+):\\s*(\\d+)$");
    private EventCodec() {}

    public static String encode(EventType type, long id) {
        if (type == null) throw new IllegalArgumentException("type");
        return type.name() + ": " + id; // canonical
    }

    public static Optional<DecodedEvent> decode(String message) {
        if (message == null) return Optional.empty();
        var m = PATTERN.matcher(message.trim());
        if (!m.matches()) return Optional.empty();
        var t = EventType.fromString(m.group(1));
        if (t == null) return Optional.empty();
        long id = Long.parseLong(m.group(2));
        return Optional.of(new DecodedEvent(t, id));
    }

    public static final class DecodedEvent {
        private final EventType type;
        private final long id;
        public DecodedEvent(EventType type, long id) { this.type = type; this.id = id; }
        public EventType getType() { return type; }
        public long getId() { return id; }
        @Override public String toString() { return type + ": " + id; }
    }
}
