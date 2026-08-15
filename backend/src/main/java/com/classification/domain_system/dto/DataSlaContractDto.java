package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class DataSlaContractDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataSlaItem {
        private String slaId;
        private String contractName;
        private String targetChannelOrDomain;
        private int latencyThresholdMs;
        private int currentLatencyMs;
        private double availabilityTargetPercent;
        private double currentAvailabilityPercent;
        private double qualityCompliancePercent;
        private String status; // MEETING_SLA, WARNING, BREACHED
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataSlaReport {
        private int totalContracts;
        private int compliantCount;
        private double overallComplianceRate;
        private List<DataSlaItem> contracts;
        private String summary;
    }
}
