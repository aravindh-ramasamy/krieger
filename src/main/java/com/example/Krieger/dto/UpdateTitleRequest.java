// src/main/java/com/example/Krieger/dto/UpdateTitleRequest.java
package com.example.Krieger.dto;

public class UpdateTitleRequest {
    private String title;

    public UpdateTitleRequest() {}

    public UpdateTitleRequest(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
