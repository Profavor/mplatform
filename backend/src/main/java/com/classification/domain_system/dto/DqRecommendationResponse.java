package com.classification.domain_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DqRecommendationResponse {
    private String id;
    private String fieldName;
    private String recommendedRuleType;
    private String reason;
    private String suggestedParameter;
    private int confidenceScore;
}
