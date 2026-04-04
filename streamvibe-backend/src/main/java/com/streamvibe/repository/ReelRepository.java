// ─── ReelRepository.java ─────────────────────────────────────────
package com.streamvibe.repository;

import com.streamvibe.entity.Reel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReelRepository extends JpaRepository<Reel, Long> {

    List<Reel> findByIsPublicTrueOrderByCreatedAtDesc();

    List<Reel> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId);

    @Modifying
    @Query("UPDATE Reel r SET r.viewCount = r.viewCount + 1 WHERE r.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
