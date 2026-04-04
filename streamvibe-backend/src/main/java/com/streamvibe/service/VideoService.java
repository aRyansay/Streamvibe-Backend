package com.streamvibe.service;

import com.streamvibe.dto.VideoDto;
import com.streamvibe.entity.User;
import com.streamvibe.entity.Video;
import com.streamvibe.repository.UserRepository;
import com.streamvibe.repository.VideoRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    public VideoService(VideoRepository videoRepository,
                        UserRepository userRepository) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
    }

    /** Public home feed — no auth required */
    public List<VideoDto.Response> getPublicFeed() {
        return videoRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a single video by id.
     * Increments view count atomically in the same transaction.
     */
    @Transactional
    @Cacheable(value = "video", key = "#id")
    public VideoDto.Response getById(Long id) {
        Video v = videoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Video not found"));

        videoRepository.incrementViewCount(id);  // atomic at DB level
        v.setViewCount(v.getViewCount() + 1);    // reflect in response only
        return toResponse(v);
    }

    /** Upload a new video */
    @Transactional
    public VideoDto.Response create(VideoDto.CreateRequest req, Long uploaderId) {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        String emoji = req.thumbnailEmoji != null ? req.thumbnailEmoji : "🎬";

        Video video = Video.builder()
                .uploader(uploader)
                .title(req.title.trim())
                .description(req.description != null ? req.description.trim() : "")
                .durationSeconds(req.durationSeconds)
                .thumbnailEmoji(emoji)
                .tags(req.tags != null ? req.tags.trim() : "")
                .viewCount(0L)
                .isPublic(true)
                .build();

        videoRepository.save(video);
        return toResponse(video);
    }

    /** Delete a video — only the owner can delete */
    @Transactional
    public void delete(Long videoId, Long requestingUserId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Video not found"));

        if (!video.getUploader().getId().equals(requestingUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete your own videos");
        }
        videoRepository.delete(video);
    }

    /** Map entity → response DTO */
    public VideoDto.Response toResponse(Video v) {
        VideoDto.Response r = new VideoDto.Response();
        r.id = v.getId();
        r.uploaderId = v.getUploader().getId();
        r.uploaderName = v.getUploader().getUsername();
        r.uploaderEmoji = v.getUploader().getAvatarEmoji();
        r.title = v.getTitle();
        r.description = v.getDescription();
        r.durationSeconds = v.getDurationSeconds();
        r.thumbnailEmoji = v.getThumbnailEmoji();
        r.tags = v.getTags();
        r.viewCount = v.getViewCount();
        r.isPublic = v.getIsPublic();
        r.createdAt = v.getCreatedAt() != null ? v.getCreatedAt().toString() : null;
        r.updatedAt = v.getUpdatedAt() != null ? v.getUpdatedAt().toString() : null;
        return r;
    }
}
