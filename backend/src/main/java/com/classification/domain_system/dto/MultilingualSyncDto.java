package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MultilingualSyncDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MissingLocaleItem {
        private UUID fieldId;
        private String fieldKey;
        private Map<String, String> currentNameMap;
        private List<String> missingLanguages;
        private String suggestedTermName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncPlanResponse {
        private UUID domainId;
        private int totalFields;
        private int missingCount;
        private List<MissingLocaleItem> missingItems;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplySyncResult {
        private UUID domainId;
        private int syncedCount;
        private String message;
    }
}
