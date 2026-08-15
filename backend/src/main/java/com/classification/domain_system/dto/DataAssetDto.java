package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

public class DataAssetDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DomainValuationItem {
        private UUID domainId;
        private String domainName;
        private long recordCount;
        private int usageFrequencyScore;
        private double dqQualityScore;
        private int connectedChannelCount;
        private String assetRating; // AAA, AA, A, BBB, BB, B, C
        private long estimatedAssetValueWon;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataAssetSummaryResponse {
        private int totalDomainsEvaluated;
        private long totalEstimatedAssetValueWon;
        private double averageQualityScore;
        private List<DomainValuationItem> domainValuations;
        private String summary;
    }
}
