package com.streamvibe.dto;

import jakarta.validation.constraints.*;

public class VideoDto {

    /** Request body for POST /api/videos */
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 255)
        public String title;

        public String description;

        @NotNull(message = "Duration is required")
        @DecimalMin(value = "0.5",  message = "Video must be at least 0.5 seconds")
        @DecimalMax(value = "72000", message = "Video cannot exceed 20 hours (72000 seconds)")
        public Float durationSeconds;

        @Size(max = 10)
        public String thumbnailEmoji;

        @Size(max = 500)
        public String tags;
    }

    /** Response body for GET /api/videos and GET /api/videos/{id} */
    public static class Response {
        public Long id;
        public Long uploaderId;
        public String uploaderName;
        public String uploaderEmoji;
        public String title;
        public String description;
        public Float durationSeconds;
        public String thumbnailEmoji;
        public String tags;
        public Long viewCount;
        public Boolean isPublic;
        public String createdAt;
        public String updatedAt;
    }
}
