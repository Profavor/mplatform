package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

public class ReferenceIntegrityDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrphanReferenceItem {
        private UUID sourceRecordId;
        private String sourceRecordCode;
        private String sourceFieldKey;
        private String targetRecordId;
        private String issueType; // TARGET_NOT_FOUND, TARGET_DELETED, TARGET_REJECTED
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IntegrityReportResponse {
        private UUID domainId;
        private long totalScannedRecords;
        private int totalReferenceFields;
        private int orphanCount;
        private int integrityScore; // 0 ~ 100
        private List<OrphanReferenceItem> violations;
    }
}
