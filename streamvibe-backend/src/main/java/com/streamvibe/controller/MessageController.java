package com.streamvibe.controller;

import com.streamvibe.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable Long messageId,
                              Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        messageService.deleteMessage(messageId, userId);
    }

    @PostMapping("/{messageId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long messageId,
                         Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        messageService.markRead(messageId, userId);
    }
}
