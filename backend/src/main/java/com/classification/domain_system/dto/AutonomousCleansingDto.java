package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

public class AutonomousCleansingDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnomalyCleansingItem {
        private String recordCode;
        private String fieldKey;
        private String anomalyValue;
        private String recommendedValue;
        private double confidenceScore;
        private String cleansingStrategy; // MEDIAN_INTERPOLATION, DOMAIN_CROSS_MAP, FORMAT_NORMALIZATION
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CleansingProposalResponse {
        private UUID domainId;
        private int totalAnomalies;
        private List<AnomalyCleansingItem> items;
        private String summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplyCleansingRequest {
        private List<String> recordCodes;
    }
}
