package com.streamvibe.service;

import com.streamvibe.dto.ConversationDto;
import com.streamvibe.entity.Conversation;
import com.streamvibe.entity.User;
import com.streamvibe.repository.ConversationRepository;
import com.streamvibe.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    public Conversation getOrCreate(Long userIdX, Long userIdY) {
        Long aId = Math.min(userIdX, userIdY);
        Long bId = Math.max(userIdX, userIdY);
        return conversationRepository
                .findByUserAIdAndUserBId(aId, bId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUserA(userRepository.getReferenceById(aId));
                    c.setUserB(userRepository.getReferenceById(bId));
                    c.setCreatedAt(LocalDateTime.now());
                    c.setLastActivityAt(LocalDateTime.now());
                    return conversationRepository.save(c);
                });
    }

    public List<ConversationDto.Summary> listForUser(Long userId) {
        return conversationRepository.findByUserAIdOrUserBId(userId, userId)
                .stream()
                .sorted(Comparator.comparing(Conversation::getLastActivityAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(conv -> {
                    User friend = conv.getUserA().getId().equals(userId)
                            ? conv.getUserB()
                            : conv.getUserA();
                    return new ConversationDto.Summary(
                            conv.getId(),
                            friend.getId(),
                            friend.getUsername(),
                            friend.getAvatarEmoji(),
                            conv.getLastActivityAt() != null ? conv.getLastActivityAt().toString() : null
                    );
                })
                .collect(Collectors.toList());
    }
}
