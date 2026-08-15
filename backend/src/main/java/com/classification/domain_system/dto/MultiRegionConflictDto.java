package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class MultiRegionConflictDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegionConflictRecord {
        private String conflictId;
        private String domainCode;
        private String recordCode;
        private String regionA; // KR_SEOUL
        private String regionB; // US_VIRGINIA
        private String fieldKey;
        private String valueA;
        private String valueB;
        private String resolvedValue;
        private String resolutionStrategy; // VECTOR_CLOCK_LWW, BUSINESS_PRIORITY_RULE
        private String status; // AUTO_RESOLVED, RESOLVED
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegionSyncReport {
        private int totalRegions;
        private int activeConflicts;
        private int autoResolvedCount;
        private List<RegionConflictRecord> conflicts;
        private String summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResolveConflictRequest {
        private String chosenValue;
    }
}
