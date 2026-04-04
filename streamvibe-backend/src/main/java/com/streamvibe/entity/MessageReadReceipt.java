package com.streamvibe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_read_receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReadReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    private User reader;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}