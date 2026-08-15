package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PipelineScheduleDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncPipelineItem {
        private String pipelineId;
        private String name;
        private UUID sourceDomainId;
        private String sourceDomainName;
        private UUID targetDomainId;
        private String targetDomainName;
        private String cronExpression;
        private boolean active;
        private LocalDateTime lastRunAt;
        private int lastSyncedCount;
        private String status; // SUCCESS, RUNNING, IDLE, FAILED
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatePipelineRequest {
        private String name;
        private UUID sourceDomainId;
        private UUID targetDomainId;
        private String cronExpression;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PipelineTriggerResponse {
        private String pipelineId;
        private int syncedCount;
        private String status;
        private String message;
    }
}
