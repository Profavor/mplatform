package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

public class SchemaCompatibilityDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompatibilityRiskItem {
        private String fieldKey;
        private String changeType; // REMOVED, MADE_REQUIRED, TYPE_CHANGED, ADDED_OPTIONAL
        private String riskLevel; // CRITICAL, WARNING, INFO
        private String impactDescription;
        private String mitigationGuide;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchemaCompatibilityReport {
        private UUID domainId;
        private String overallCompatibility; // COMPATIBLE, BREAKING_CHANGE
        private int riskScore; // 0 ~ 100
        private List<CompatibilityRiskItem> risks;
        private String summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchemaChangeSimulationRequest {
        private String proposedChanges;
    }
}
