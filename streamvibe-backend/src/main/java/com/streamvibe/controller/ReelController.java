package com.streamvibe.controller;

import com.streamvibe.dto.ReelDto;
import com.streamvibe.service.ReelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reels")
public class ReelController {

    private final ReelService reelService;

    public ReelController(ReelService reelService) {
        this.reelService = reelService;
    }

    @GetMapping
    public List<ReelDto.Response> getAll() {
        return reelService.getPublicFeed();
    }

    @GetMapping("/{id}")
    public ReelDto.Response getById(@PathVariable Long id) {
        return reelService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReelDto.Response create(@Valid @RequestBody ReelDto.CreateRequest req,
                                   Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return reelService.create(req, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        reelService.delete(id, userId);
    }
}
