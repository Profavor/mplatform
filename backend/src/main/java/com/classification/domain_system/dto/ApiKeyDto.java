package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ApiKeyDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiKeyItem {
        private String keyId;
        private String name;
        private String maskedKey;
        private List<String> scopes;
        private String allowedIpsCsv;
        private boolean active;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateApiKeyRequest {
        private String name;
        private List<String> scopes;
        private String allowedIpsCsv;
        private int validDays;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateApiKeyResponse {
        private String keyId;
        private String rawApiKey; // Exposed only once upon creation
        private String maskedKey;
        private List<String> scopes;
        private LocalDateTime expiresAt;
        private String message;
    }
}
