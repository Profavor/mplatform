package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class RejectionAnalyticsDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RejectionCategoryCount {
        private String category;
        private int count;
        private double percentage;
        private String guide;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RejectionAnalysisResponse {
        private int totalRejections;
        private List<RejectionCategoryCount> topCategories;
        private List<String> recommendedChecklist;
        private String summary;
    }
}
