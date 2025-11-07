package com.example.Krieger.dto;

import java.time.Instant;

public class SummaryResult {
    private final Long id;
    private final String title;
    private final Long authorId;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String preview;

    public SummaryResult(Long id, String title, Long authorId, Instant createdAt, Instant updatedAt, String preview) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.preview = preview;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Long getAuthorId() { return authorId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getPreview() { return preview; }
}
