package com.classification.domain_system.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartMappingRecommendationDto {

    private String sourceField;
    private String targetFieldKey;
    private String targetFieldName;
    private int confidenceScore; // 0 ~ 100
    private String matchReason;
    private String recommendedSpel;
}
