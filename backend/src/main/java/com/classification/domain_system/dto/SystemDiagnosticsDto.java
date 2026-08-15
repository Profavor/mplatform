package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class SystemDiagnosticsDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComponentHealthItem {
        private String componentName;
        private String status; // UP, DEGRADED, DOWN
        private long latencyMs;
        private String details;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GlobalSystemDiagnosticsResponse {
        private String overallStatus; // HEALTHY, WARNING, CRITICAL
        private LocalDateTime checkedAt;
        private double averageLatencyMs;
        private List<ComponentHealthItem> components;
        private String summary;
    }
}
