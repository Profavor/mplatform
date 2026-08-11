package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataProfilingResponse;
import com.classification.domain_system.dto.DqRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DqRecommendationService {

    private final DataProfilingService dataProfilingService;

    public List<DqRecommendationResponse> getRecommendations(UUID domainId) {
        List<DataProfilingResponse> profiles = dataProfilingService.profileDomainData(domainId);
        List<DqRecommendationResponse> recommendations = new ArrayList<>();

        for (DataProfilingResponse profile : profiles) {
            String field = profile.getFieldName();
            
            // Heuristic 1: If null ratio is exactly 0% but it's not marked as required, recommend NOT_NULL rule.
            if (profile.getNullRatio() == 0.0 && profile.getTotalCount() > 10) {
                recommendations.add(DqRecommendationResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .fieldName(field)
                        .recommendedRuleType("NOT_NULL")
                        .reason("해당 필드의 100%가 값이 존재합니다. 필수값(Not Null) 규칙 추가를 제안합니다.")
                        .confidenceScore(95)
                        .build());
            }

            // Heuristic 2: If cardinality is very low compared to total, recommend ENUM/ALLOWED_VALUES rule.
            if (profile.getTotalCount() > 50 && profile.getCardinality() > 0 && profile.getCardinality() <= 5) {
                String topValuesList = String.join(",", profile.getTopValues().keySet());
                recommendations.add(DqRecommendationResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .fieldName(field)
                        .recommendedRuleType("ALLOWED_VALUES")
                        .reason("고유값이 " + profile.getCardinality() + "개 뿐입니다. 제한된 값만 입력되도록 허용값 규칙 추가를 제안합니다.")
                        .suggestedParameter(topValuesList)
                        .confidenceScore(85)
                        .build());
            }
            
            // Heuristic 3: Check for ID or Code fields
            if (field.toLowerCase().endsWith("id") || field.toLowerCase().endsWith("code")) {
                if (profile.getCardinality() == profile.getTotalCount() && profile.getTotalCount() > 5) {
                    recommendations.add(DqRecommendationResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .fieldName(field)
                        .recommendedRuleType("UNIQUE")
                        .reason("해당 필드의 모든 값이 고유합니다. Unique 제약조건 추가를 제안합니다.")
                        .confidenceScore(90)
                        .build());
                }
            }
        }
        
        return recommendations;
    }
}
