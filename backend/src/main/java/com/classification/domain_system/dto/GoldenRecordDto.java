package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GoldenRecordDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoldenRecordBuildRequest {
        private List<UUID> targetRecordIds;
        private String priorityStrategy; // LATEST, SOURCE_PRIORITY (ERP > CRM > WEB)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoldenFieldChoice {
        private String fieldKey;
        private UUID chosenRecordId;
        private String chosenRecordCode;
        private Object chosenValue;
        private String sourceSystem;
        private String selectionReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoldenRecordPreviewResponse {
        private List<String> candidateRecordCodes;
        private List<GoldenFieldChoice> fieldChoices;
        private Map<String, Object> assembledData;
        private int confidenceScore;
        private String summary;
    }
}
