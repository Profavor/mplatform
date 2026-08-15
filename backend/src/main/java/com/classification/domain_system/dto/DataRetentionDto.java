package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DataRetentionDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpiredRecordScanResponse {
        private UUID domainId;
        private int retentionYears;
        private int expiredCount;
        private List<String> expiredRecordCodes;
        private String summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurgeRequest {
        private int retentionYears;
        private String purgeType; // ANONYMIZE, HARD_DELETE
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurgeExecutionResponse {
        private UUID domainId;
        private int purgedCount;
        private String certificateId;
        private LocalDateTime timestamp;
        private String summary;
    }
}
