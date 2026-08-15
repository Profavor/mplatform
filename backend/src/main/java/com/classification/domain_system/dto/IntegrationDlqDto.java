package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class IntegrationDlqDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DlqItemResponse {
        private UUID logId;
        private UUID channelId;
        private String channelName;
        private UUID recordId;
        private String recordCode;
        private String eventType;
        private String status;
        private String errorMessage;
        private int retryCount;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DlqRetryRequest {
        private List<UUID> logIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DlqRetryResult {
        private int successCount;
        private int failureCount;
        private String message;
    }
}
