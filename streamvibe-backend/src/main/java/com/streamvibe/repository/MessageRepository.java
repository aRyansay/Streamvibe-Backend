package com.streamvibe.repository;

import com.streamvibe.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdAndIsDeletedFalseOrderBySentAtAsc(Long conversationId, Pageable pageable);

    @Query("""
        SELECT m FROM Message m
        JOIN m.conversation c
        WHERE m.isDeleted = false
          AND (m.type = 'VIDEO_REF' OR m.type = 'REEL_REF')
          AND ((c.userA.id = :userId AND m.sender.id <> :userId)
            OR (c.userB.id = :userId AND m.sender.id <> :userId))
        ORDER BY m.sentAt DESC
    """)
    List<Message> findInboxForUser(@Param("userId") Long userId);
}
