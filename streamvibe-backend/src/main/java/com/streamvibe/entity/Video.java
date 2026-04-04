package com.streamvibe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Video implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Duration in seconds as a float.
     * Covers 0.5s (30-second minimum) to 72000s (20 hours).
     * Constraint enforced both at DB level (schema.sql CHECK) and
     * application level (VideoService.validate).
     */
    @Column(name = "duration_seconds", nullable = false)
    private Float durationSeconds;

    @Column(name = "thumbnail_emoji", length = 10)
    private String thumbnailEmoji = "🎬";

    @Column(length = 500)
    private String tags;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
