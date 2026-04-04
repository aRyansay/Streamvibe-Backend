package com.streamvibe.dto;

import java.io.Serializable;

public record SuggestedUserDto (
        Long id,
        String name,
        String email
) {}
