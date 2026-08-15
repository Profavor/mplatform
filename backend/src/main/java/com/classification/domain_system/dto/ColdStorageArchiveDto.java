package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ColdStorageArchiveDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArchivePackageInfo {
        private String archiveId;
        private LocalDateTime createdAt;
        private String archiveName;
        private String checksumSha256;
        private int domainCount;
        private int recordCount;
        private long totalSizeBytes;
        private String compressionRatio;
        private String status; // FROZEN, RESTORE_TESTED
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateArchiveRequest {
        private String archiveName;
        private boolean encrypt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DrSimulationResult {
        private String archiveId;
        private boolean integrityVerified;
        private long drDurationMs;
        private int domainsRestored;
        private int recordsRestored;
        private String message;
    }
}
