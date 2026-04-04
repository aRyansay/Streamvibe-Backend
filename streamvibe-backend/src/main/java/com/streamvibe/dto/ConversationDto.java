package com.streamvibe.dto;

public class ConversationDto {

    public record Summary(
            Long id,
            Long friendId,
            String friendName,
            String friendAvatarEmoji,
            String lastActivityAt
    ) {}
}
