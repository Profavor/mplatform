package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

public class MasterOrchestratorDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeatureModuleStatus {
        private int featureNo;
        private String category; // DQ_QUALITY, SECURITY_COMPLIANCE, WORKFLOW_APPROVAL, INTEGRATION_PIPELINE, SCHEMA_LIFECYCLE, AI_INNOVATION
        private String featureName;
        private String status; // ONLINE_HEALTHY, ACTIVE
        private int healthScore;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MasterOrchestratorSummary {
        private int totalFeatures; // 50
        private int healthyFeatures; // 50
        private String systemMaturityLevel;
        private Map<String, Integer> categoryDistribution;
        private List<FeatureModuleStatus> modules;
        private String summary;
    }
}
