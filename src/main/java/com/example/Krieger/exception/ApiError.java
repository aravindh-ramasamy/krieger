package com.example.Krieger.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ApiError {
    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldErrorEntry> fieldErrors = new ArrayList<>();

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public List<FieldErrorEntry> getFieldErrors() { return fieldErrors; }

    public void addFieldError(String field, String message) {
        this.fieldErrors.add(new FieldErrorEntry(field, message));
    }

    public static class FieldErrorEntry {
        private String field;
        private String message;

        public FieldErrorEntry() {}
        public FieldErrorEntry(String field, String message) {
            this.field = field; this.message = message;
        }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
