package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class DomainSnapshotDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SnapshotCreateRequest {
        private String snapshotName;
        private String versionTag;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SnapshotResponse {
        private UUID snapshotId;
        private UUID domainId;
        private String snapshotName;
        private String versionTag;
        private int recordCount;
        private String createdBy;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SnapshotRestoreResponse {
        private UUID snapshotId;
        private int restoredRecords;
        private String status;
        private String message;
    }
}
