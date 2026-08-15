package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataProfilingReportDto {

    private UUID domainId;
    private String domainName;
    private long totalRecords;
    private LocalDateTime scannedAt;
    private List<FieldProfile> fieldProfiles;
    private List<OutlierRecord> outliers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldProfile {
        private String fieldKey;
        private String fieldName;
        private String fieldType;
        private long totalCount;
        private long nullCount;
        private double nullRate; // %
        private long distinctCount;
        private double uniquenessRatio; // %
        private Double minValue;
        private Double maxValue;
        private Double avgValue;
        private Double iqrLowerBound;
        private Double iqrUpperBound;
        private long outlierCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OutlierRecord {
        private UUID recordId;
        private String fieldKey;
        private Object value;
        private String reason; // e.g. "IQR 초과 (상한: 100, 입력: 250)"
        private Double zScore;
    }
}
