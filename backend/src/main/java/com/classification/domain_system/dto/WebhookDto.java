package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class WebhookDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionCreateRequest {
        private String name;
        private String targetUrl;
        private String secretKey;
        private List<String> events;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionResponse {
        private UUID id;
        private String name;
        private String targetUrl;
        private String secretKey;
        private List<String> events;
        private boolean active;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WebhookTestResult {
        private UUID subscriptionId;
        private String targetUrl;
        private int statusCode;
        private String signatureHeader;
        private boolean success;
        private String message;
    }
}
