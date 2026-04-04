package com.streamvibe.service;

import com.streamvibe.dto.ShareDto;
import com.streamvibe.entity.*;
import com.streamvibe.entity.Share.ContentType;
import com.streamvibe.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShareService {

    private final ShareRepository shareRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final ReelRepository reelRepository;
    private final FriendshipRepository friendshipRepository;

    public ShareService(ShareRepository shareRepository,
                        UserRepository userRepository,
                        VideoRepository videoRepository,
                        ReelRepository reelRepository,
                        FriendshipRepository friendshipRepository) {
        this.shareRepository = shareRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.reelRepository = reelRepository;
        this.friendshipRepository = friendshipRepository;
    }

    /**
     * Forward a video or reel to a friend.
     *
     * Rules:
     *  - sender and receiver must be accepted friends
     *  - content must exist
     *  - Only a reference (type + id) is stored — no copy
     */
    @Transactional
    public ShareDto.Response create(ShareDto.CreateRequest req, Long senderId) {
        if (senderId.equals(req.receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot share content with yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Sender not found"));
        User receiver = userRepository.findById(req.receiverId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Receiver not found"));

        // Verify they are friends
        friendshipRepository.findBetween(senderId, req.receiverId)
                .filter(f -> f.getStatus() == Friendship.FriendStatus.ACCEPTED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only share with friends"));

        // Validate content exists
        ContentType type;
        try {
            type = ContentType.valueOf(req.contentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid content type — use VIDEO or REEL");
        }

        if (type == ContentType.VIDEO) {
            videoRepository.findById(req.contentId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Video not found"));
        } else {
            reelRepository.findById(req.contentId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Reel not found"));
        }

        Share share = Share.builder()
                .sender(sender)
                .receiver(receiver)
                .contentType(type)
                .contentId(req.contentId)
                .isDeleted(false)
                .build();

        shareRepository.save(share);
        return toResponse(share);
    }

    /**
     * Get the "chat thread" between the logged-in user and one friend.
     * Returns non-deleted shares in chronological order (oldest first).
     */
    public List<ShareDto.Response> getChat(Long userId, Long friendId) {
        return shareRepository.findChatBetween(userId, friendId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all received (inbox) shares for the logged-in user.
     */
    public List<ShareDto.Response> getInbox(Long userId) {
        return shareRepository.findInboxByReceiverId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Soft-delete a share from the receiver's inbox.
     *
     * This sets is_deleted = true. The original video/reel is UNTOUCHED.
     * Only the receiver can delete a share from their inbox.
     */
    @Transactional
    public void delete(Long shareId, Long userId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Share not found"));

        if (!share.getReceiver().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only remove shares from your own inbox");
        }

        share.setIsDeleted(true);
        shareRepository.save(share);
    }

    /** Map entity → response DTO, resolving the referenced content title */
    private ShareDto.Response toResponse(Share s) {
        ShareDto.Response r = new ShareDto.Response();
        r.id = s.getId();
        r.senderId = s.getSender().getId();
        r.senderName = s.getSender().getUsername();
        r.receiverId = s.getReceiver().getId();
        r.receiverName = s.getReceiver().getUsername();
        r.contentType = s.getContentType().name();
        r.contentId = s.getContentId();
        r.isDeleted = s.getIsDeleted();
        r.sharedAt = s.getSharedAt() != null ? s.getSharedAt().toString() : null;

        // Resolve title/emoji/duration from the original content
        if (s.getContentType() == ContentType.VIDEO) {
            videoRepository.findById(s.getContentId()).ifPresentOrElse(v -> {
                r.contentTitle = v.getTitle();
                r.contentEmoji = v.getThumbnailEmoji();
                r.contentDuration = v.getDurationSeconds();
                r.uploaderName = v.getUploader().getUsername();
            }, () -> {
                r.contentTitle = "[Deleted video]";
                r.contentEmoji = "🎬";
            });
        } else {
            reelRepository.findById(s.getContentId()).ifPresentOrElse(reel -> {
                r.contentTitle = reel.getCaption();
                r.contentEmoji = reel.getEmoji();
                r.contentDuration = reel.getDurationSeconds();
                r.uploaderName = reel.getUploader().getUsername();
            }, () -> {
                r.contentTitle = "[Deleted reel]";
                r.contentEmoji = "📱";
            });
        }

        return r;
    }
}
