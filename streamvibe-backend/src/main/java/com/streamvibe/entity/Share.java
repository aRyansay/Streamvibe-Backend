package com.streamvibe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shares")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /**
     * The type of the shared content.
     * VIDEO → look up in videos table by contentId
     * REEL  → look up in reels table by contentId
     *
     * We store only a reference (type + id), never a copy.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    /**
     * Soft-delete flag.
     * true  → hidden from receiver's inbox (but original video/reel untouched)
     * false → visible
     */
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "shared_at", updatable = false)
    private LocalDateTime sharedAt;

    public enum ContentType {
        VIDEO, REEL
    }
}
