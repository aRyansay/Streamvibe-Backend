package com.streamvibe.service;

import com.streamvibe.dto.AuthRequest;
import com.streamvibe.dto.AuthResponse;
import com.streamvibe.entity.User;
import com.streamvibe.repository.UserRepository;
import com.streamvibe.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new account.
     * Validates uniqueness of email and username, hashes password, saves user,
     * returns a JWT immediately so the user is logged in after registration.
     */
    public AuthResponse register(AuthRequest.Register req) {
        if (userRepository.existsByEmail(req.email)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Email already used" );
        }
        if (userRepository.existsByUsername(req.username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username already taken");
        }

        User user = User.builder()
                .username(req.username.trim())
                .email(req.email.toLowerCase().trim())
                .password(passwordEncoder.encode(req.password))
                .avatarEmoji("👤")
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(user.getId(), user.getUsername(),
                user.getEmail(), user.getAvatarEmoji(), token);
    }

    /**
     * Login with email + password.
     * Returns a fresh JWT on success.
     */
    public AuthResponse login(AuthRequest.Login req) {
        User user = userRepository.findByEmail(req.email.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password" ));
        if (!passwordEncoder.matches(req.password, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(user.getId(), user.getUsername(),
                user.getEmail(), user.getAvatarEmoji(), token);
    }
}
