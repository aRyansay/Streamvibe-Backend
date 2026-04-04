package com.streamvibe.repository;

import com.streamvibe.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShareRepository extends JpaRepository<Share, Long> {

    /**
     * All non-deleted shares exchanged between two specific users, newest first.
     * Used to render a "chat" thread between user A and user B.
     */
    @Query("SELECT s FROM Share s " +
           "WHERE s.isDeleted = false " +
           "AND ((s.sender.id = :userId AND s.receiver.id = :friendId) " +
           "  OR (s.sender.id = :friendId AND s.receiver.id = :userId)) " +
           "ORDER BY s.sharedAt ASC")
    List<Share> findChatBetween(@Param("userId") Long userId,
                                @Param("friendId") Long friendId);

    /**
     * All non-deleted shares received by a user — their full inbox.
     */
    @Query("SELECT s FROM Share s " +
           "WHERE s.receiver.id = :userId AND s.isDeleted = false " +
           "ORDER BY s.sharedAt DESC")
    List<Share> findInboxByReceiverId(@Param("userId") Long userId);

    /**
     * Count of unread (non-deleted) shares received from a specific friend.
     * Used for badge counts in the friend list.
     */
    @Query("SELECT COUNT(s) FROM Share s " +
           "WHERE s.receiver.id = :userId AND s.sender.id = :senderId " +
           "AND s.isDeleted = false")
    long countReceivedFrom(@Param("userId") Long userId,
                           @Param("senderId") Long senderId);
}
