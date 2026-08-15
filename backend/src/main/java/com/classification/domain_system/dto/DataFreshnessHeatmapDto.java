package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class DataFreshnessHeatmapDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DomainFreshnessItem {
        private String domainCode;
        private String domainName;
        private String lastUpdatedTime;
        private int freshnessSlaMinutes;
        private int delayMinutes;
        private int freshnessScore; // 0 ~ 100
        private String status; // FRESH, DELAYED, STALE
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FreshnessHeatmapResponse {
        private int overallFreshnessScore;
        private int totalDomains;
        private int staleCount;
        private List<DomainFreshnessItem> domains;
        private String summary;
    }
}
