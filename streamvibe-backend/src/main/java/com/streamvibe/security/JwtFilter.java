package com.streamvibe.security;

import com.streamvibe.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Intercepts every HTTP request.
 * If an "Authorization: Bearer <token>" header is present and valid,
 * the user is authenticated in the Spring Security context.
 * Public endpoints (GET /api/videos, GET /api/reels) still pass
 * through — SecurityConfig handles their permit-all rules.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtUtil.isValid(token)) {
            Long userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);

            // Load user to verify they still exist
            userRepository.findByEmail(email).ifPresent(user -> {
                // Create a simple authentication token (no roles/authorities needed here)
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId,          // principal = userId (Long)
                                null,
                                Collections.emptyList()
                        );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract the token from "Authorization: Bearer <token>" header.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
