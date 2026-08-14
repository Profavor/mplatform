package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataProfilingResponse;
import com.classification.domain_system.dto.DqRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DqRecommendationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{1,2}-?\\d{3,4}-?\\d{4}$");
    private static final Pattern BIZ_NO_PATTERN = Pattern.compile("^\\d{3}-?\\d{2}-?\\d{5}$");

    private final DataProfilingService dataProfilingService;

    public List<DqRecommendationResponse> getRecommendations(UUID domainId) {
        List<DataProfilingResponse> profiles = dataProfilingService.profileDomainData(domainId);
        List<DqRecommendationResponse> recommendations = new ArrayList<>();

        for (DataProfilingResponse profile : profiles) {
            String field = profile.getFieldName();
            Map<String, Long> topValues = profile.getTopValues() != null ? profile.getTopValues() : Collections.emptyMap();
            
            // Heuristic 1: If null ratio is exactly 0%, recommend NOT_NULL rule.
            if (profile.getNullRatio() == 0.0 && profile.getTotalCount() > 5) {
                recommendations.add(DqRecommendationResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .fieldName(field)
                        .recommendedRuleType("NOT_NULL")
                        .reason("해당 필드의 100%가 값이 존재합니다. 필수값(Not Null) 규칙 추가를 제안합니다.")
                        .confidenceScore(95)
                        .build());
            }

            // Heuristic 2: Pattern Recognition (REGEX) - Email, Phone, Business No
            if (!topValues.isEmpty()) {
                long emailMatches = 0;
                long phoneMatches = 0;
                long bizMatches = 0;
                long numericMatches = 0;
                double minNum = Double.MAX_VALUE;
                double maxNum = -Double.MAX_VALUE;

                for (String val : topValues.keySet()) {
                    if (val == null || val.isBlank()) continue;
                    String trimmed = val.trim();
                    if (EMAIL_PATTERN.matcher(trimmed).matches()) emailMatches++;
                    if (PHONE_PATTERN.matcher(trimmed).matches()) phoneMatches++;
                    if (BIZ_NO_PATTERN.matcher(trimmed).matches()) bizMatches++;

                    try {
                        double parsed = Double.parseDouble(trimmed);
                        numericMatches++;
                        minNum = Math.min(minNum, parsed);
                        maxNum = Math.max(maxNum, parsed);
                    } catch (NumberFormatException ignored) {}
                }

                int sampleSize = topValues.size();
                if (sampleSize > 0) {
                    if ((double) emailMatches / sampleSize >= 0.7 || field.toLowerCase().contains("email")) {
                        recommendations.add(DqRecommendationResponse.builder()
                                .id(UUID.randomUUID().toString())
                                .fieldName(field)
                                .recommendedRuleType("REGEX")
                                .suggestedParameter("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
                                .reason("이메일 형식의 데이터가 다수 감지되었습니다. 이메일 형식 유효성 검증 규칙(REGEX)을 제안합니다.")
                                .confidenceScore(92)
                                .build());
                    } else if ((double) phoneMatches / sampleSize >= 0.7 || field.toLowerCase().contains("phone") || field.toLowerCase().contains("tel")) {
                        recommendations.add(DqRecommendationResponse.builder()
                                .id(UUID.randomUUID().toString())
                                .fieldName(field)
                                .recommendedRuleType("REGEX")
                                .suggestedParameter("^0\\d{1,2}-?\\d{3,4}-?\\d{4}$")
                                .reason("전화번호/휴대폰 형식 데이터가 감지되었습니다. 연락처 형식 유효성 검증 규칙(REGEX)을 제안합니다.")
                                .confidenceScore(90)
                                .build());
                    } else if ((double) bizMatches / sampleSize >= 0.7 || field.toLowerCase().contains("biz") || field.toLowerCase().contains("saup")) {
                        recommendations.add(DqRecommendationResponse.builder()
                                .id(UUID.randomUUID().toString())
                                .fieldName(field)
                                .recommendedRuleType("REGEX")
                                .suggestedParameter("^\\d{3}-?\\d{2}-?\\d{5}$")
                                .reason("사업자등록번호 형식 데이터가 감지되었습니다. 사업자번호 유효성 검증 규칙(REGEX)을 제안합니다.")
                                .confidenceScore(90)
                                .build());
                    } else if ((double) numericMatches / sampleSize >= 0.8 && minNum != Double.MAX_VALUE && maxNum != -Double.MAX_VALUE) {
                        double safeMin = minNum >= 0 ? 0 : minNum * 1.5;
                        double safeMax = maxNum * 1.5;
                        recommendations.add(DqRecommendationResponse.builder()
                                .id(UUID.randomUUID().toString())
                                .fieldName(field)
                                .recommendedRuleType("RANGE")
                                .suggestedParameter(String.format("%.0f,%.0f", safeMin, safeMax))
                                .reason(String.format("수치형 데이터(범위: %.0f ~ %.0f)가 감지되었습니다. 허용 범위 규칙(RANGE)을 제안합니다.", minNum, maxNum))
                                .confidenceScore(85)
                                .build());
                    }
                }
            }

            // Heuristic 3: Low cardinality -> ALLOWED_VALUES rule
            if (profile.getTotalCount() > 20 && profile.getCardinality() > 0 && profile.getCardinality() <= 5) {
                String topValuesList = String.join(",", topValues.keySet());
                recommendations.add(DqRecommendationResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .fieldName(field)
                        .recommendedRuleType("ALLOWED_VALUES")
                        .reason("고유값이 " + profile.getCardinality() + "개 뿐입니다. 제한된 값만 입력되도록 허용값 규칙 추가를 제안합니다.")
                        .suggestedParameter(topValuesList)
                        .confidenceScore(88)
                        .build());
            }
            
            // Heuristic 4: Check for ID or Code fields -> UNIQUE
            if (field.toLowerCase().endsWith("id") || field.toLowerCase().endsWith("code") || field.toLowerCase().endsWith("no")) {
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

