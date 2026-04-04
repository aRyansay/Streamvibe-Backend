package com.streamvibe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Column(nullable = false, length = 500)
    private String caption;

    /**
     * Duration in seconds as a float.
     * Covers 0.01s to 180s (3 minutes).
     */
    @Column(name = "duration_seconds", nullable = false)
    private Float durationSeconds;

    @Column(length = 10)
    private String emoji = "📱";

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
