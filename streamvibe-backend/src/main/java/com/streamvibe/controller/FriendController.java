package com.streamvibe.controller;

import com.streamvibe.dto.SuggestedUserDto;
import com.streamvibe.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * All endpoints require JWT auth.
 *
 * GET  /api/friends                     — your accepted friends list
 * GET  /api/friends/requests/pending    — incoming requests to you
 * GET  /api/friends/requests/sent       — outgoing requests from you
 * POST /api/friends/request/{targetId}  — send a friend request
 * PUT  /api/friends/request/{id}/accept — accept a request
 * PUT  /api/friends/request/{id}/decline— decline a request
 * DELETE /api/friends/{friendshipId}    — remove a friend
 * GET  /api/friends/search?q=username   — search users to add
 */
@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public List<Map<String, Object>> getFriends(Authentication auth) {
        return friendService.getFriends(currentUserId(auth));
    }

    @GetMapping("/requests/pending")
    public List<Map<String, Object>> getPendingRequests(Authentication auth) {
        return friendService.getPendingRequests(currentUserId(auth));
    }

    @GetMapping("/requests/sent")
    public List<Map<String, Object>> getSentRequests(Authentication auth) {
        return friendService.getSentRequests(currentUserId(auth));
    }

    @PostMapping("/request/{targetId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> sendRequest(@PathVariable Long targetId,
                                           Authentication auth) {
        return friendService.sendRequest(currentUserId(auth), targetId);
    }

    @PutMapping("/request/{id}/accept")
    public Map<String, Object> acceptRequest(@PathVariable Long id,
                                             Authentication auth) {
        return friendService.acceptRequest(id, currentUserId(auth));
    }

    @PutMapping("/request/{id}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineRequest(@PathVariable Long id, Authentication auth) {
        friendService.declineRequest(id, currentUserId(auth));
    }

    @DeleteMapping("/{friendshipId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable Long friendshipId, Authentication auth) {
        friendService.removeFriend(friendshipId, currentUserId(auth));
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchUsers(@RequestParam String q,
                                                 Authentication auth) {
        return friendService.searchUsers(q, currentUserId(auth));
    }

    @GetMapping("/mutual")
    public List<SuggestedUserDto> searchMutualUsers(
            Authentication auth) {

        return friendService.suggestFriends(currentUserId(auth));
    }

    private Long currentUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

}
