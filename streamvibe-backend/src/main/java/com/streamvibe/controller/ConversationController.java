package com.streamvibe.controller;

import com.streamvibe.dto.ConversationDto;
import com.streamvibe.dto.MessageDto;
import com.streamvibe.service.ConversationService;
import com.streamvibe.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    public ConversationController(ConversationService conversationService,
                                  MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @GetMapping
    public List<ConversationDto.Summary> listConversations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return conversationService.listForUser(userId);
    }

    @GetMapping("/{friendId}/messages")
    public List<MessageDto.Response> getMessages(@PathVariable Long friendId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "50") int size,
                                                 Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return messageService.listMessages(userId, friendId, page, size);
    }

    @PostMapping("/{friendId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto.Response sendMessage(@PathVariable Long friendId,
                                           @Valid @RequestBody MessageDto.CreateRequest req,
                                           Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return messageService.send(userId, friendId, req);
    }
}
