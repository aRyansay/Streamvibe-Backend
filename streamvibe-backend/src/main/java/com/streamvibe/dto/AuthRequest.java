// ════════════════════════════════════════════════════════════════
//  StreamVibe — All DTOs
//  Each DTO class is in the com.streamvibe.dto package.
//  Separate them into individual files in production.
// ════════════════════════════════════════════════════════════════

// ─── AuthRequest.java ───────────────────────────────────────────
package com.streamvibe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequest {

    public static class Register {
        @NotBlank public String username;
        @NotBlank @Email public String email;
        @NotBlank @Size(min = 6) public String password;
    }

    public static class Login {
        @NotBlank @Email public String email;
        @NotBlank public String password;
    }
}

// ─── AuthResponse.java ──────────────────────────────────────────
// package com.streamvibe.dto;
//
// public class AuthResponse {
//     public Long id;
//     public String username;
//     public String email;
//     public String avatarEmoji;
//     public String token;          // JWT
//
//     public AuthResponse(Long id, String username, String email,
//                         String avatarEmoji, String token) {
//         this.id = id;
//         this.username = username;
//         this.email = email;
//         this.avatarEmoji = avatarEmoji;
//         this.token = token;
//     }
// }
