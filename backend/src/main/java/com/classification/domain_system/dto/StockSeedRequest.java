package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSeedRequest {
    private Boolean clearExisting; // true to clean existing records in STOCK domain before seeding
    private List<String> markets;  // Optional filter: e.g. ["KOSPI", "KOSDAQ", "US_MARKET"]
    private Integer limitPerMarket; // Optional limit per market
    private List<java.util.Map<String, Object>> customRows; // Optional custom rows to seed directly
}
