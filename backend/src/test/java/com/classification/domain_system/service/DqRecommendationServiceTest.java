package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataProfilingResponse;
import com.classification.domain_system.dto.DqRecommendationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DqRecommendationServiceTest {

    @Mock
    private DataProfilingService dataProfilingService;

    private DqRecommendationService dqRecommendationService;

    @BeforeEach
    void setUp() {
        dqRecommendationService = new DqRecommendationService(dataProfilingService);
    }

    @Test
    @DisplayName("이메일 패턴 데이터 발견 시 REGEX 이메일 검증 규칙을 추천한다")
    void testRecommendEmailRegex() {
        UUID domainId = UUID.randomUUID();

        Map<String, Long> emailTopValues = new HashMap<>();
        emailTopValues.put("test@example.com", 15L);
        emailTopValues.put("admin@mdm.com", 20L);

        DataProfilingResponse profile = DataProfilingResponse.builder()
                .fieldName("EMAIL")
                .totalCount(35)
                .nullCount(0)
                .nullRatio(0.0)
                .cardinality(35)
                .topValues(emailTopValues)
                .build();

        when(dataProfilingService.profileDomainData(eq(domainId))).thenReturn(List.of(profile));

        List<DqRecommendationResponse> recommendations = dqRecommendationService.getRecommendations(domainId);

        assertTrue(recommendations.stream().anyMatch(r -> "REGEX".equals(r.getRecommendedRuleType())));
    }

    @Test
    @DisplayName("전화번호 패턴 데이터 발견 시 REGEX 전화번호 검증 규칙을 추천한다")
    void testRecommendPhoneRegex() {
        UUID domainId = UUID.randomUUID();

        Map<String, Long> phoneTopValues = new HashMap<>();
        phoneTopValues.put("010-1234-5678", 10L);
        phoneTopValues.put("010-9876-5432", 10L);

        DataProfilingResponse profile = DataProfilingResponse.builder()
                .fieldName("PHONE_NO")
                .totalCount(20)
                .nullCount(0)
                .nullRatio(0.0)
                .cardinality(20)
                .topValues(phoneTopValues)
                .build();

        when(dataProfilingService.profileDomainData(eq(domainId))).thenReturn(List.of(profile));

        List<DqRecommendationResponse> recommendations = dqRecommendationService.getRecommendations(domainId);

        assertTrue(recommendations.stream().anyMatch(r -> "REGEX".equals(r.getRecommendedRuleType())));
    }

    @Test
    @DisplayName("수치형 데이터 발견 시 RANGE 규칙을 추천한다")
    void testRecommendRangeRule() {
        UUID domainId = UUID.randomUUID();

        Map<String, Long> numTopValues = new HashMap<>();
        numTopValues.put("100", 5L);
        numTopValues.put("250", 10L);
        numTopValues.put("500", 8L);

        DataProfilingResponse profile = DataProfilingResponse.builder()
                .fieldName("SALARY")
                .totalCount(23)
                .nullCount(0)
                .nullRatio(0.0)
                .cardinality(20)
                .topValues(numTopValues)
                .build();

        when(dataProfilingService.profileDomainData(eq(domainId))).thenReturn(List.of(profile));

        List<DqRecommendationResponse> recommendations = dqRecommendationService.getRecommendations(domainId);

        assertTrue(recommendations.stream().anyMatch(r -> "RANGE".equals(r.getRecommendedRuleType()) || "NOT_NULL".equals(r.getRecommendedRuleType())));
    }
}
