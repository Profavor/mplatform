package com.classification.domain_system.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordBatchUpsertResponse {
    private int totalCount;
    private int createdCount;
    private int updatedCount;
    private int failedCount;
    private long executionTimeMs;
    private List<RecordBatchUpsertItemResult> items;
}
