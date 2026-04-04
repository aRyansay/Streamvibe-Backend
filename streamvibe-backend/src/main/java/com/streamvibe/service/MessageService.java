package com.streamvibe.service;

import com.streamvibe.dto.MessageDto;
import com.streamvibe.entity.*;
import com.streamvibe.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageReadReceiptRepository readReceiptRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final VideoRepository videoRepository;
    private final ReelRepository reelRepository;

    public MessageService(ConversationService conversationService,
                          ConversationRepository conversationRepository,
                          MessageRepository messageRepository,
                          MessageReadReceiptRepository readReceiptRepository,
                          UserRepository userRepository,
                          FriendshipRepository friendshipRepository,
                          VideoRepository videoRepository,
                          ReelRepository reelRepository) {
        this.conversationService = conversationService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.readReceiptRepository = readReceiptRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.videoRepository = videoRepository;
        this.reelRepository = reelRepository;
    }

    @Transactional
    public MessageDto.Response send(Long senderId, Long receiverId, MessageDto.CreateRequest req) {
        if (senderId.equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot message yourself");
        }

        friendshipRepository.findBetween(senderId, receiverId)
                .filter(f -> f.getStatus() == Friendship.FriendStatus.ACCEPTED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only message friends"));

        MessageType type;
        try {
            type = MessageType.valueOf(req.type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid message type");
        }

        if (type == MessageType.TEXT) {
            if (req.textBody == null || req.textBody.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Message text is required");
            }
        } else {
            if (req.contentId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Content ID is required");
            }
            if (type == MessageType.VIDEO_REF) {
                videoRepository.findById(req.contentId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Video not found"));
            } else if (type == MessageType.REEL_REF) {
                reelRepository.findById(req.contentId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Reel not found"));
            }
        }

        Conversation conv = conversationService.getOrCreate(senderId, receiverId);

        Message msg = Message.builder()
                .conversation(conv)
                .sender(userRepository.getReferenceById(senderId))
                .type(type)
                .textBody(type == MessageType.TEXT ? req.textBody.trim() : null)
                .contentId(type == MessageType.TEXT ? null : req.contentId)
                .isDeleted(false)
                .sentAt(LocalDateTime.now())
                .build();

        conv.setLastActivityAt(LocalDateTime.now());
        conversationRepository.save(conv);

        Message saved = messageRepository.save(msg);
        return toResponse(saved, senderId, receiverId);
    }

    public List<MessageDto.Response> listMessages(Long userId, Long friendId, int page, int size) {
        Conversation conv = conversationService.getOrCreate(userId, friendId);
        Page<Message> messages = messageRepository
                .findByConversationIdAndIsDeletedFalseOrderBySentAtAsc(conv.getId(),
                        PageRequest.of(page, size));
        return messages.stream()
                .map(m -> toResponse(m, userId, friendId))
                .collect(Collectors.toList());
    }

        public List<MessageDto.Response> listInbox(Long userId) {
        return messageRepository.findInboxForUser(userId)
            .stream()
            .map(m -> {
                Long friendId = m.getConversation().getUserA().getId().equals(userId)
                    ? m.getConversation().getUserB().getId()
                    : m.getConversation().getUserA().getId();
                return toResponse(m, userId, friendId);
            })
            .collect(Collectors.toList());
        }

    @Transactional
    public void deleteMessage(Long messageId, Long requesterId) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message not found"));
        if (!msg.getSender().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete your own messages");
        }
        msg.setDeleted(true);
        messageRepository.save(msg);
    }

    @Transactional
    public void markRead(Long messageId, Long readerId) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message not found"));
        readReceiptRepository.findByMessageIdAndReaderId(messageId, readerId)
                .orElseGet(() -> {
                    MessageReadReceipt rr = MessageReadReceipt.builder()
                            .message(msg)
                            .reader(userRepository.getReferenceById(readerId))
                            .readAt(LocalDateTime.now())
                            .build();
                    return readReceiptRepository.save(rr);
                });
    }

    private MessageDto.Response toResponse(Message msg, Long viewerId, Long friendId) {
        MessageDto.Response r = new MessageDto.Response();
        r.id = msg.getId();
        r.conversationId = msg.getConversation().getId();
        r.senderId = msg.getSender().getId();
        r.senderName = msg.getSender().getUsername();
        r.receiverId = friendId;
        r.type = msg.getType().name();
        r.textBody = msg.getTextBody();
        r.contentId = msg.getContentId();
        r.isDeleted = msg.isDeleted();
        r.sentAt = msg.getSentAt() != null ? msg.getSentAt().toString() : null;

        if (msg.getType() == MessageType.VIDEO_REF) {
            videoRepository.findById(msg.getContentId()).ifPresentOrElse(v -> {
                r.contentTitle = v.getTitle();
                r.contentEmoji = v.getThumbnailEmoji();
                r.contentDuration = v.getDurationSeconds();
                r.uploaderName = v.getUploader().getUsername();
            }, () -> {
                r.contentTitle = "[Deleted video]";
                r.contentEmoji = "🎬";
            });
        } else if (msg.getType() == MessageType.REEL_REF) {
            reelRepository.findById(msg.getContentId()).ifPresentOrElse(reel -> {
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
