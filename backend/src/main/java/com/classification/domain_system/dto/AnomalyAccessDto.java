package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class AnomalyAccessDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnomalySecurityEvent {
        private String eventId;
        private String userId;
        private String username;
        private String sourceIp;
        private String actionType;
        private String threatLevel; // CRITICAL, HIGH, MEDIUM, LOW
        private String details;
        private LocalDateTime timestamp;
        private boolean blocked;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnomalyDetectionSummaryResponse {
        private int threatLevelScore;
        private int activeThreatCount;
        private List<AnomalySecurityEvent> events;
        private String summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlockUserRequest {
        private String userId;
        private String reason;
    }
}
