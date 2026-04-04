package com.streamvibe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageDto {

    public static class CreateRequest {
        @NotBlank(message = "Message type is required")
        public String type; // TEXT | VIDEO_REF | REEL_REF

        public String textBody;

        public Long contentId;
    }

    public static class Response {
        public Long id;
        public Long conversationId;
        public Long senderId;
        public String senderName;
        public Long receiverId;
        public String type;
        public String textBody;
        public Long contentId;
        public String contentTitle;
        public String contentEmoji;
        public Float contentDuration;
        public String uploaderName;
        public Boolean isDeleted;
        public String sentAt;
    }
}
