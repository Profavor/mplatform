package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationChannelStatsDto {
    private UUID channelId;
    private String channelName;
    private String channelCode;
    private String type;
    private long totalCount;
    private long successCount;
    private long failCount;
    private double successRate;
    private LocalDateTime lastExecutedAt;
    private String lastStatus;
    private String healthStatus; // HEALTHY, WARNING, CRITICAL, IDLE
}
