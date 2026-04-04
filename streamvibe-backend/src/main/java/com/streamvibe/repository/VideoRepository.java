package com.streamvibe.repository;

import com.streamvibe.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    /** Public home feed — all public videos, newest first */
    List<Video> findByIsPublicTrueOrderByCreatedAtDesc();

    /** Videos uploaded by a specific user */
    List<Video> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId);

    /** Increment view count atomically (avoids race conditions in multi-instance deploys) */
    @Modifying
    @Query("UPDATE Video v SET v.viewCount = v.viewCount + 1 WHERE v.id = :id")
    void incrementViewCount(@Param("id") Long id);

}
