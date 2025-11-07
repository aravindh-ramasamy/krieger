package com.example.Krieger.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    /** Main content body (TEXT). */
    @Column(columnDefinition = "TEXT")
    private String body;

    // Many-to-one relationship with author
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private Author author;

    @Column(name = "\"references\"")
    private String references;

    /** Timestamps for auditing */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /** Lifecycle hooks to manage timestamps without vendor-specific annotations */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /* ---------------- Convenience accessors for API layers ---------------- */

    /** Alias for body to match controller helpers (e.g., CSV export uses getContent()). */
    @Transient
    public String getContent() {
        return this.body;
    }

    /** Expose the author's id without serializing the full author. */
    @Transient
    @JsonIgnore
    public Long getAuthorId() {
        return (author != null) ? author.getId() : null;
    }
}
