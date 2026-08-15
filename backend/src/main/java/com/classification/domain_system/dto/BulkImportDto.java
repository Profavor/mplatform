package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BulkImportDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private UUID domainId;
        private UUID nodeId;
        private String fileName;
        private List<Map<String, Object>> rows;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Progress {
        private UUID jobId;
        private UUID domainId;
        private String fileName;
        private String status;
        private int totalRows;
        private int processedRows;
        private int successCount;
        private int errorCount;
        private double progressPercentage;
        private List<ErrorDetail> errorDetails;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetail {
        private int rowNumber;
        private String recordKey;
        private String errorMessage;
    }
}
