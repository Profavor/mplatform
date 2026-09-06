package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SchemaCompatibilityDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompatibilityRiskItem {
        private String fieldKey;
        private String changeType; // REMOVED, MADE_REQUIRED, TYPE_CHANGED, ADDED_OPTIONAL, RENAMED
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldImpactSimulationRequest {
        private UUID fieldDefinitionId;
        private String changeType; // DROP, MAKE_REQUIRED, TYPE_CHANGE, RENAME
        private String newType;    // STRING, NUMBER, DATE, BOOLEAN, ENUM, etc.
        private Boolean newRequired;
        private String newKey;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldImpactReport {
        private UUID fieldDefinitionId;
        private String fieldKey;
        private Map<String, String> fieldName;
        private String changeType;
        private String compatibilityStatus; // BREAKING, WARNING, SAFE
        private int riskScore; // 0 ~ 100

        // 1. Data Impact (레코드 데이터 사용 현황)
        private long totalDomainRecords;
        private long affectedRecordCount;
        private double affectedRecordPercentage;

        // 2. Downstream Impact (다운스트림 및 연계 채널 / DQ 규칙)
        private List<String> affectedChannels;
        private List<String> affectedDqRules;

        // 3. Risk Items & Guidance
        private List<CompatibilityRiskItem> risks;
        private List<String> mitigationGuides;
        private String summary;
    }
}
