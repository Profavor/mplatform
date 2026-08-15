package com.classification.domain_system.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordBulkReclassifyResult {
    private int totalCount;
    private int successCount;
    private int failureCount;
    @Builder.Default
    private List<String> errorMessages = new ArrayList<>();
}
