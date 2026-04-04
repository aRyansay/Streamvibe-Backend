package com.streamvibe.controller;

import com.streamvibe.dto.AuthRequest;
import com.streamvibe.dto.AuthResponse;
import com.streamvibe.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * POST /api/auth/register  — create account, returns JWT
 * POST /api/auth/login     — login, returns fresh JWT
 *
 * Logout is handled ENTIRELY on the frontend by discarding the token.
 * JWT is stateless — there is no server-side session to invalidate.
 * For token revocation in production, use a Redis blocklist.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRequest.Register req) {

        return authService.register(req);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest.Login req) {
//        return authService.login(req);
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(response);
    }
}
