package com.streamvibe.controller;

import com.streamvibe.dto.MessageDto;
import com.streamvibe.dto.ShareDto;
import com.streamvibe.service.MessageService;
import com.streamvibe.service.ShareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All endpoints require JWT auth.
 *
 * POST   /api/shares              — forward a video or reel to a friend
 * GET    /api/shares/inbox        — your received shares (full inbox)
 * GET    /api/shares/chat/{friendId} — thread between you and one friend
 * DELETE /api/shares/{id}         — soft-delete from your inbox
 */
@RestController
@RequestMapping("/api/shares")
public class ShareController {

    private final ShareService shareService;
    private final MessageService messageService;

    public ShareController(ShareService shareService,
                           MessageService messageService) {
        this.shareService = shareService;
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShareDto.Response create(@Valid @RequestBody ShareDto.CreateRequest req,
                                    Authentication auth) {
        Long senderId = (Long) auth.getPrincipal();
        return shareService.create(req, senderId);
    }

    @GetMapping("/inbox")
    public List<MessageDto.Response> getInbox(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return messageService.listInbox(userId);
    }

    @GetMapping("/chat/{friendId}")
    public List<ShareDto.Response> getChat(@PathVariable Long friendId,
                                           Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return shareService.getChat(userId, friendId);
    }

    @DeleteMapping("/{shareId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long shareId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        shareService.delete(shareId, userId);
    }
}
