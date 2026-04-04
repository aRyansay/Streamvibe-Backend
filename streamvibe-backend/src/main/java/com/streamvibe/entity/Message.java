package com.streamvibe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType type;       // TEXT | VIDEO_REF | REEL_REF

    @Column(name = "text_body", columnDefinition = "TEXT")
    private String textBody;        // non-null when type=TEXT

    @Column(name = "content_id")
    private Long contentId;         // non-null when type=VIDEO_REF or REEL_REF

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}