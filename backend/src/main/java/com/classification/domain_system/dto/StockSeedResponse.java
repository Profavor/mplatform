package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSeedResponse {
    private UUID domainId;
    private String domainName;
    private int totalSeeded;
    private int totalCreated;
    private int totalMerged;
    private int totalDeleted;
    private Map<String, Integer> seededByMarket;
    private String message;
}
