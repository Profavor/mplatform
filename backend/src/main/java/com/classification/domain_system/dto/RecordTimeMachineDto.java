package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecordTimeMachineDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VersionInfo {
        private int version;
        private String changeType;
        private String changedBy;
        private LocalDateTime changedAt;
        private String changeReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldDiffItem {
        private String fieldKey;
        private String fieldName;
        private String v1Value;
        private String v2Value;
        private String diffStatus; // ADDED, MODIFIED, REMOVED, UNCHANGED
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeMachineDiffResponse {
        private UUID recordId;
        private String recordCode;
        private int v1;
        private int v2;
        private Map<String, Object> v1Data;
        private Map<String, Object> v2Data;
        private List<FieldDiffItem> fieldDiffs;
        private List<VersionInfo> allVersions;
    }
}
