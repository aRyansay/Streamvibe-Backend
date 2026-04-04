package com.streamvibe.service;

import com.streamvibe.dto.SuggestedUserDto;
import com.streamvibe.entity.Friendship;
import com.streamvibe.entity.Friendship.FriendStatus;
import com.streamvibe.entity.User;
import com.streamvibe.repository.FriendshipRepository;
import com.streamvibe.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendService(FriendshipRepository friendshipRepository,
                         UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    /** Get all accepted friends of the logged-in user */
    public List<Map<String, Object>> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findAcceptedByUserId(userId);
        return friendships.stream().map(f -> {
            // The "friend" is whichever side isn't the current user
            User friend = f.getRequester().getId().equals(userId)
                    ? f.getAddressee()
                    : f.getRequester();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("friendshipId", f.getId());
            m.put("userId", friend.getId());
            m.put("username", friend.getUsername());
            m.put("avatarEmoji", friend.getAvatarEmoji());
            return m;
        }).collect(Collectors.toList());
    }

    /** Get incoming pending friend requests for userId */
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        return friendshipRepository
                .findByAddresseeIdAndStatus(userId, FriendStatus.PENDING)
                .stream().map(f -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("requestId", f.getId());
                    m.put("fromUserId", f.getRequester().getId());
                    m.put("fromUsername", f.getRequester().getUsername());
                    m.put("fromEmoji", f.getRequester().getAvatarEmoji());
                    m.put("sentAt", f.getCreatedAt().toString());
                    return m;
                }).collect(Collectors.toList());
    }

    /** Get outgoing pending requests sent by userId */
    public List<Map<String, Object>> getSentRequests(Long userId) {
        return friendshipRepository
                .findByRequesterIdAndStatus(userId, FriendStatus.PENDING)
                .stream().map(f -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("requestId", f.getId());
                    m.put("toUserId", f.getAddressee().getId());
                    m.put("toUsername", f.getAddressee().getUsername());
                    m.put("toEmoji", f.getAddressee().getAvatarEmoji());
                    m.put("sentAt", f.getCreatedAt().toString());
                    return m;
                }).collect(Collectors.toList());
    }

    /**
     * Send a friend request from requesterId → targetId.
     * Rules:
     *  - Cannot add yourself
     *  - Cannot send if any record already exists (PENDING, ACCEPTED, or DECLINED)
     */
    @Transactional
    @CacheEvict(cacheNames = "friendSuggestions", allEntries = true)
    public Map<String, Object> sendRequest(Long requesterId, Long targetId) {
        if (requesterId.equals(targetId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot send a friend request to yourself");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        User addressee = userRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Target user not found"));

        friendshipRepository.findBetween(requesterId, targetId).ifPresent(existing -> {
            String msg = switch (existing.getStatus()) {
                case ACCEPTED -> "You are already friends";
                case PENDING  -> "A friend request already exists";
                case DECLINED -> "This request was declined — try again later";
            };
            throw new ResponseStatusException(HttpStatus.CONFLICT, msg);
        });

        Friendship f = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendStatus.PENDING)
                .build();
        friendshipRepository.save(f);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("requestId", f.getId());
        res.put("status", "PENDING");
        res.put("message", "Friend request sent to " + addressee.getUsername());
        return res;
    }

    /**
     * Accept a pending request.
     * Only the addressee (receiver) can accept.
     */
    @Transactional
    @CacheEvict(cacheNames = "friendSuggestions", allEntries = true)
    public Map<String, Object> acceptRequest(Long requestId, Long userId) {
        Friendship f = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));

        if (!f.getAddressee().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the recipient can accept this request");
        }
        if (f.getStatus() != FriendStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Request is not in PENDING state");
        }

        f.setStatus(FriendStatus.ACCEPTED);
        friendshipRepository.save(f);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "ACCEPTED");
        res.put("friendUsername", f.getRequester().getUsername());
        return res;
    }

    /**
     * Decline a pending request.
     * Only the addressee can decline.
     */
    @Transactional
    @CacheEvict(cacheNames = "friendSuggestions", allEntries = true)
    public void declineRequest(Long requestId, Long userId) {
        Friendship f = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));
        if (!f.getAddressee().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the recipient can decline this request");
        }
        f.setStatus(FriendStatus.DECLINED);
        friendshipRepository.save(f);
    }

    /**
     * Remove an accepted friendship.
     * Either party can remove.
     */
    @Transactional
    @CacheEvict(cacheNames = "friendSuggestions", allEntries = true)
    public void removeFriend(Long friendshipId, Long userId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Friendship not found"));
        boolean involved = f.getRequester().getId().equals(userId)
                        || f.getAddressee().getId().equals(userId);
        if (!involved) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Not your friendship to remove");
        }
        friendshipRepository.delete(f);
    }

    /**
     * Search users by username — returns non-friend users (for "add friend" search).
     */
    public int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j - 1], // replace
                            Math.min(dp[i - 1][j], dp[i][j - 1]) // delete/insert
                    );
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    public int bestMatchScore(String query, String username) {
        int min = Integer.MAX_VALUE;

        for (int i = 0; i <= username.length() - 1; i++) {
            for (int j = i + 1; j <= username.length(); j++) {
                String sub = username.substring(i, j);

                int dist = levenshtein(query, sub);
                min = Math.min(min, dist);
            }
        }

        // Bonus boosts
        if (username.startsWith(query)) min -= 2;
        if (username.contains(query)) min -= 1;

        return min;
    }

    @Cacheable(value = "usersBySearch", key = "#query + '_' + #currentUserId")
    public List<Map<String, Object>> searchUsers(String query, Long currentUserId) {
        List<Long> friendIds = getFriends(currentUserId)
                .stream()
                .map(m -> (Long) m.get("userId"))
                .collect(Collectors.toList());

        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .filter(u -> !friendIds.contains(u.getId()))
                .map(u -> {
                    String username = u.getUsername().toLowerCase();
                    String q = query.toLowerCase();

                    int score = bestMatchScore(q, username);

                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("username", u.getUsername());
                    m.put("avatarEmoji", u.getAvatarEmoji());
                    m.put("score", score);

                    return m;
                })
                .sorted(Comparator.comparingInt(m -> (int) m.get("score")))
                .limit(3)
                .peek(m -> m.remove("score"))
                .collect(Collectors.toList());
    }


    ///
    private Map<Long, Set<Long>> buildGraph(List<Friendship> friendships) {
        Map<Long, Set<Long>> graph = new HashMap<>();

        for (Friendship f : friendships) {
            Long u1 = f.getRequester().getId();
            Long u2 = f.getAddressee().getId();

            graph.computeIfAbsent(u1, k -> new HashSet<>()).add(u2);
            graph.computeIfAbsent(u2, k -> new HashSet<>()).add(u1);
        }

        return graph;
    }
    public Set<Long> getDirectFriendIds(Long userId) {
        List<Map<String, Object>> friends = getFriends(userId);

        return friends.stream()
                .map(f -> ((Number) f.get("userId")).longValue())
                .collect(Collectors.toSet());
    }


    public List<SuggestedUserDto> suggestFriends(Long userId) {

        // Step 1: Get direct friends
        Set<Long> directFriends = getDirectFriendIds(userId);

        if (directFriends.isEmpty()) {
            return List.of();
        }

        // Step 2: Fetch all needed data in ONE go
        List<Friendship> friendships =
                friendshipRepository.findGraphForUser(userId, directFriends);

        // Step 3: Build graph
        Map<Long, Set<Long>> graph = buildGraph(friendships);

        // Step 4: BFS (2-hop)
        Map<Long, Integer> mutualCount = new HashMap<>();

        for (Long friend : directFriends) {
            for (Long fof : graph.getOrDefault(friend, Set.of())) {

                if (fof.equals(userId) || directFriends.contains(fof)) continue;

                mutualCount.put(fof, mutualCount.getOrDefault(fof, 0) + 1);
            }
        }

        if (mutualCount.isEmpty()) {
            return List.of();
        }

        // Step 5: Sort by mutual friends
        List<Long> suggestedIds = mutualCount.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .map(Map.Entry::getKey)
            .limit(10)
            .toList();

        // Step 6: Fetch user details in batch (avoid N+1)
        List<User> users = userRepository.findAllByIdIn(suggestedIds);
        Map<Long, User> userById = users.stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        // Step 7: Map to DTOs in ranked order
        return suggestedIds.stream()
            .map(id -> {
                User u = userById.get(id);
                if (u == null) {
                return null;
                }
                return new SuggestedUserDto(u.getId(), u.getUsername(), u.getEmail());
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
