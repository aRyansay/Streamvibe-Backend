package com.streamvibe.dto;

public class AuthResponse {
    public Long id;
    public String username;
    public String email;
    public String avatarEmoji;
    public String token;          // JWT — store in memory, not localStorage

    public AuthResponse(Long id, String username, String email,
                        String avatarEmoji, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarEmoji = avatarEmoji;
        this.token = token;
    }
}
