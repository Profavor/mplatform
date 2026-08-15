package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

public class GovernanceMaturityDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaturityDimension {
        private String dimensionName;
        private int currentScore; // 0 ~ 100
        private String level; // Level 1 ~ Level 5
        private String strengths;
        private String gapAndRoadmap;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GovernanceMaturityReport {
        private String overallLevel; // Level 4 (Optimized)
        private int overallScore; // 0 ~ 100
        private Map<String, Double> kpiSummary; // completeness, timeliness, consistency, validity
        private List<MaturityDimension> dimensions;
        private String summary;
    }
}
