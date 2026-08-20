package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProfilingResponse {
    private String fieldKey;
    private String fieldName;
    private long totalCount;
    private long nullCount;
    private double nullRatio;
    private long cardinality;
    private Map<String, Long> topValues;
}
