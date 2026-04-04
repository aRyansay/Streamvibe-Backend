package com.streamvibe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;   // lower id always goes here

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;   // higher id always goes here

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;
}