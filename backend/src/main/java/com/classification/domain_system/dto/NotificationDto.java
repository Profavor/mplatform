package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationResponse {
        private UUID id;
        private String userId;
        private String title;
        private String message;
        private String type;
        private String linkUrl;
        private boolean isRead;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationCreateRequest {
        private String userId;
        private String title;
        private String message;
        private String type;
        private String linkUrl;
    }
}
