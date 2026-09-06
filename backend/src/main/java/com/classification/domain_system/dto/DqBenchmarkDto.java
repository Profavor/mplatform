package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DqBenchmarkDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenchmarkItem {
        private UUID domainId;
        private String domainCode;
        private Map<String, String> domainName;
        private double score;
        private String grade; // "A" (>=90), "B" (>=80), "C" (>=70), "D" (<70)
        private long totalRecords;
        private long totalViolations;
        private long errorCount;
        private long warningCount;
        private long ruleCount;
        private String riskLevel; // "HEALTHY" (>=90), "WARNING" (>=80), "HIGH_RISK" (<80)
        private double trendDelta; // e.g. +2.5, -1.2 compared to previous snapshot
        private Map<String, Double> dimensionScores; // completeness, uniqueness, validity, accuracy
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewResponse {
        private double averageScore;
        private int totalMonitoredDomains;
        private BenchmarkItem highestDomain;
        private BenchmarkItem lowestDomain;
        private long totalViolations;
        private int highRiskDomainCount;
        private List<BenchmarkItem> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private LocalDateTime recordedAt;
        private double score;
        private long totalViolations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiDomainTrend {
        private UUID domainId;
        private String domainCode;
        private Map<String, String> domainName;
        private List<TrendPoint> dataPoints;
    }
}
