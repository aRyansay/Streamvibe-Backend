package com.streamvibe.service;

import com.streamvibe.dto.ReelDto;
import com.streamvibe.entity.Reel;
import com.streamvibe.entity.User;
import com.streamvibe.repository.ReelRepository;
import com.streamvibe.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReelService {

    private final ReelRepository reelRepository;
    private final UserRepository userRepository;

    public ReelService(ReelRepository reelRepository,
                       UserRepository userRepository) {
        this.reelRepository = reelRepository;
        this.userRepository = userRepository;
    }

    public List<ReelDto.Response> getPublicFeed() {
        return reelRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReelDto.Response getById(Long id) {
        Reel r = reelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reel not found"));
        reelRepository.incrementViewCount(id);
        r.setViewCount(r.getViewCount() + 1);
        return toResponse(r);
    }

    @Transactional
    public ReelDto.Response create(ReelDto.CreateRequest req, Long uploaderId) {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Reel reel = Reel.builder()
                .uploader(uploader)
                .caption(req.caption.trim())
                .durationSeconds(req.durationSeconds)
                .emoji(req.emoji != null ? req.emoji : "📱")
                .tags(req.tags != null ? req.tags.trim() : "")
                .viewCount(0L)
                .isPublic(true)
                .build();

        reelRepository.save(reel);
        return toResponse(reel);
    }

    @Transactional
    public void delete(Long reelId, Long requestingUserId) {
        Reel reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reel not found"));
        if (!reel.getUploader().getId().equals(requestingUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete your own reels");
        }
        reelRepository.delete(reel);
    }

    public ReelDto.Response toResponse(Reel r) {
        ReelDto.Response res = new ReelDto.Response();
        res.id = r.getId();
        res.uploaderId = r.getUploader().getId();
        res.uploaderName = r.getUploader().getUsername();
        res.caption = r.getCaption();
        res.durationSeconds = r.getDurationSeconds();
        res.emoji = r.getEmoji();
        res.tags = r.getTags();
        res.viewCount = r.getViewCount();
        res.createdAt = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;
        return res;
    }
}
