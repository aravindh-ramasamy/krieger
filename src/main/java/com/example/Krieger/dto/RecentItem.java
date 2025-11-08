// src/main/java/com/example/Krieger/dto/RecentItem.java
package com.example.Krieger.dto;

public class RecentItem {
    private final Long id;
    private final String title;
    private final Long authorId;
    private final String updatedAt; // ISO-8601 string

    public RecentItem(Long id, String title, Long authorId, String updatedAt) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Long getAuthorId() { return authorId; }
    public String getUpdatedAt() { return updatedAt; }
}
