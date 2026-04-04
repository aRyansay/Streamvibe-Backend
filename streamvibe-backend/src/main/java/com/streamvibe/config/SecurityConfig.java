package com.streamvibe.config;

import com.streamvibe.security.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security rules:
 *
 * PUBLIC  (no JWT needed):
 *   POST /api/auth/**          — register & login
 *   GET  /api/videos           — home feed (like YouTube public feed)
 *   GET  /api/videos/{id}      — video detail page
 *   GET  /api/reels            — reel feed
 *   GET  /api/reels/{id}       — single reel
 *
 * PRIVATE (JWT required):
 *   POST   /api/videos         — upload
 *   DELETE /api/videos/{id}    — delete own video
 *   POST   /api/reels          — upload reel
 *   GET    /api/friends/**     — friends list & requests
 *   POST   /api/friends/**     — send/accept requests
 *   DELETE /api/friends/**     — remove friend
 *   GET    /api/shares/**      — inbox
 *   POST   /api/shares         — forward content
 *   DELETE /api/shares/{id}    — soft-delete from inbox
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${allowed.origins}")
    private String allowedOrigins;

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Public read-only content

                .requestMatchers(HttpMethod.GET, "/api/videos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reels/**").permitAll()
                // Everything else requires a valid JWT
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
