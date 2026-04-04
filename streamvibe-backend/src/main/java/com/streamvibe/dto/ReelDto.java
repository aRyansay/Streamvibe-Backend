package com.streamvibe.dto;

import jakarta.validation.constraints.*;

public class ReelDto {

    public static class CreateRequest {
        @NotBlank(message = "Caption is required")
        @Size(max = 500)
        public String caption;

        @NotNull(message = "Duration is required")
        @DecimalMin(value = "0.01", message = "Reel must be at least 0.01 seconds")
        @DecimalMax(value = "180",  message = "Reel cannot exceed 3 minutes (180 seconds)")
        public Float durationSeconds;

        @Size(max = 10)
        public String emoji;

        @Size(max = 500)
        public String tags;
    }

    public static class Response {
        public Long id;
        public Long uploaderId;
        public String uploaderName;
        public String caption;
        public Float durationSeconds;
        public String emoji;
        public String tags;
        public Long viewCount;
        public String createdAt;
    }
}
