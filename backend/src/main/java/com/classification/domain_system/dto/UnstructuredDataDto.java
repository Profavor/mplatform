package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

public class UnstructuredDataDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExtractedFieldItem {
        private String fieldKey;
        private String extractedValue;
        private double confidenceScore;
        private String sourceSnippet;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExtractionResponse {
        private UUID domainId;
        private int rawTextLength;
        private double overallConfidence;
        private String suggestedRecordCode;
        private List<ExtractedFieldItem> fields;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExtractFromTextRequest {
        private String rawText;
    }
}
