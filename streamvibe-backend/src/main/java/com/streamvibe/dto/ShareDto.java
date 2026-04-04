package com.streamvibe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ShareDto {

    public static class CreateRequest {
        @NotNull(message = "Receiver ID is required")
        public Long receiverId;

        @NotBlank(message = "Content type is required — VIDEO or REEL")
        public String contentType;

        @NotNull(message = "Content ID is required")
        public Long contentId;
    }

    public static class Response {
        public Long id;
        public Long senderId;
        public String senderName;
        public Long receiverId;
        public String receiverName;
        public String contentType;
        public Long contentId;
        // Resolved from the referenced video/reel at query time
        public String contentTitle;
        public String contentEmoji;
        public Float contentDuration;
        public String uploaderName;
        public Boolean isDeleted;
        public String sharedAt;
    }
}
