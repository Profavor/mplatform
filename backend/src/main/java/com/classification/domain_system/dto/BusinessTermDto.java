package com.classification.domain_system.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

public class BusinessTermDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusinessTermResponse {
        private UUID id;
        private Map<String, String> termName;
        private String termCode;
        private UUID domainId;
        private String abbreviation;
        private String synonyms;
        private String dataType;
        private String sensitivityLevel;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusinessTermCreateRequest {
        private Map<String, String> termName;
        private String termCode;
        private UUID domainId;
        private String abbreviation;
        private String synonyms;
        private String dataType;
        private String sensitivityLevel;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TermRecommendation {
        private BusinessTermResponse term;
        private double similarityScore; // 0.0 ~ 1.0
        private String matchReason;
    }
}
