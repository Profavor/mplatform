package com.classification.domain_system.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordBatchUpsertItemResult {
    private int index;
    private String businessKey;
    private String businessKeyValue;
    private UUID recordId;
    private String action; // "CREATED", "UPDATED", "FAILED", "SKIPPED"
    private String status; // "ACTIVE", "PENDING_APPROVAL"
    private String errorMessage;
}
