package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationMetricsDto {

    private UUID channelId;
    private String channelName;
    private String channelType;
    private String healthStatus; // HEALTHY, DEGRADED, UNHEALTHY
    private long totalRequests;
    private long successCount;
    private long failCount;
    private long dlqCount;
    private double successRate;
    private long avgLatencyMs;
    private Long lastPingLatencyMs;
    private LocalDateTime lastPingAt;
    private String lastPingMessage;
    private List<HourlyStat> hourlyStats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HourlyStat {
        private String timeSlot; // "HH:00"
        private long successCount;
        private long failCount;
        private long dlqCount;
    }
}
