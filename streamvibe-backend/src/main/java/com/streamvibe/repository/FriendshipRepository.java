package com.streamvibe.repository;

import com.streamvibe.entity.Friendship;
import com.streamvibe.entity.Friendship.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Get all accepted friendships for a user (either side of the relationship).
     */
        @Query("SELECT f FROM Friendship f " +
            "JOIN FETCH f.requester " +
            "JOIN FETCH f.addressee " +
            "WHERE f.status = 'ACCEPTED' " +
            "AND (f.requester.id = :userId OR f.addressee.id = :userId)")
    List<Friendship> findAcceptedByUserId(@Param("userId") Long userId);

    /**
     * Incoming pending requests — requests sent TO this user.
     */
    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendStatus status);

    /**
     * Outgoing pending requests — requests sent BY this user.
     */
    List<Friendship> findByRequesterIdAndStatus(Long requesterId, FriendStatus status);

    /**
     * Check if any friendship record exists between two users regardless of direction.
     * Used to prevent duplicate requests.
     */
    @Query("SELECT f FROM Friendship f " +
           "WHERE (f.requester.id = :a AND f.addressee.id = :b) " +
           "   OR (f.requester.id = :b AND f.addressee.id = :a)")
    Optional<Friendship> findBetween(@Param("a") Long a, @Param("b") Long b);

    @Query("""
    SELECT f FROM Friendship f
    JOIN FETCH f.requester
    JOIN FETCH f.addressee
    WHERE f.status = 'ACCEPTED'
    AND (
        f.requester.id = :userId OR 
        f.addressee.id = :userId OR
        f.requester.id IN :friendIds OR
        f.addressee.id IN :friendIds
    )
""")
    List<Friendship> findGraphForUser(@Param("userId") Long userId,
                                      @Param("friendIds") Set<Long> friendIds);
}
