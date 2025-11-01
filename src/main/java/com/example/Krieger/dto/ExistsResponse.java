package com.example.Krieger.dto;

public class ExistsResponse {
    private final boolean exists;

    public ExistsResponse(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() {
        return exists;
    }
}
