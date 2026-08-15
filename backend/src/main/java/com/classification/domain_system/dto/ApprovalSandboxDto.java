package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApprovalSandboxDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SandboxPreviewResponse {
        private UUID approvalRequestId;
        private String requesterName;
        private String targetType; // RECORD, SCHEMA, NODE, etc.
        private String actionType; // CREATE, UPDATE, DELETE
        private List<TargetRecordPreview> targetRecords;
        private String summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TargetRecordPreview {
        private UUID recordId;
        private String recordCode;
        private Map<String, Object> currentData;
        private Map<String, Object> simulatedData;
        private List<RecordTimeMachineDto.FieldDiffItem> fieldDiffs;
    }
}
