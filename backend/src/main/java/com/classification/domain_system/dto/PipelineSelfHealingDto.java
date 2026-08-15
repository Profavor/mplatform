package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class PipelineSelfHealingDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HealingActionLog {
        private String actionId;
        private String pipelineChannel;
        private String errorType; // SCHEMA_MISMATCH, NETWORK_TIMEOUT, PAYLOAD_CORRUPTION
        private String diagnosedCause;
        private String healingStrategy; // PAYLOAD_TRANSFORMATION, BACKOFF_RETRY, TRAFFIC_REROUTING
        private int recoveredCount;
        private String status; // RECOVERED, AUTO_RESOLVED
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PipelineHealingReport {
        private int totalIncidents;
        private int autoHealedCount;
        private double healingSuccessRate;
        private List<HealingActionLog> actions;
        private String summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TriggerHealingRequest {
        private String pipelineChannel;
    }
}
