package com.streamvibe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who sent the friend request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /** The user who received the friend request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status = FriendStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum FriendStatus {
        PENDING, ACCEPTED, DECLINED
    }
}
