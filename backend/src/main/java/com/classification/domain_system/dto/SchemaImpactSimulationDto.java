package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

public class SchemaImpactSimulationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimulationRequest {
        private String fieldKey;
        private String action; // DELETE, CHANGE_TYPE, SET_REQUIRED
        private String newType; // NUMBER, STRING, DATE, BOOLEAN etc.
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimulationResponse {
        private String fieldKey;
        private String action;
        private int safetyScore; // 0 ~ 100
        private String riskLevel; // SAFE, LOW, MEDIUM, HIGH, CRITICAL
        private long populatedRecordCount;
        private long nullRecordCount;
        private long totalRecordCount;
        private List<String> affectedChannels;
        private List<String> affectedDqRules;
        private String riskSummary;
        private List<String> recommendations;
    }
}
