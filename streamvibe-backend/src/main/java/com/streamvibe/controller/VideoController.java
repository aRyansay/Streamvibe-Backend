package com.streamvibe.controller;

import com.streamvibe.dto.VideoDto;
import com.streamvibe.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GET    /api/videos         — public home feed (no auth)
 * GET    /api/videos/{id}    — single video detail + view count increment (no auth)
 * POST   /api/videos         — upload new video (JWT required)
 * DELETE /api/videos/{id}    — delete own video (JWT required, owner only)
 */
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    public List<VideoDto.Response> getAll() {
        return videoService.getPublicFeed();
    }

    @GetMapping("/{id}")
    public VideoDto.Response getById(@PathVariable Long id) {
        return videoService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VideoDto.Response create(@Valid @RequestBody VideoDto.CreateRequest req,
                                    Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return videoService.create(req, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        videoService.delete(id, userId);
    }
}
