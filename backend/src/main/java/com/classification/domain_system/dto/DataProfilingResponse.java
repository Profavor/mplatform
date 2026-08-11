package com.classification.domain_system.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class DataProfilingResponse {
    private String fieldName;
    private long totalCount;
    private long nullCount;
    private double nullRatio;
    private long cardinality;
    private Map<String, Long> topValues;
}
