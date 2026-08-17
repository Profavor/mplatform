package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CdcStreamDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CdcEventItem {
        private String eventId;
        private LocalDateTime timestamp;
        private String operation; // c (create), u (update), d (delete)
        private String domainCode;
        private String recordCode;
        private String sourceConnector;
        private Map<String, Object> beforePayload;
        private Map<String, Object> afterPayload;
        private List<String> changedFields;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CdcStreamResponse {
        private List<CdcEventItem> events;
        private long activeOffset;
        private double eventsPerSecond;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimulateCdcRequest {
        private String operation;
        private String recordCode;
        private Map<String, Object> payload;
    }
}
