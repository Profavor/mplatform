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
public class SensitiveDataStatsDto {

    private Map<String, Long> dailyTrends;
    private Map<String, Long> targetTypeRatios;
    private Map<String, Long> topUsers;
}
