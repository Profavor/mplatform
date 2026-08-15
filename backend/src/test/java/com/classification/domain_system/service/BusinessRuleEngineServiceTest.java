package com.classification.domain_system.service;

import com.classification.domain_system.dto.BusinessRuleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class BusinessRuleEngineServiceTest {

    private BusinessRuleEngineService ruleEngineService;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        ruleEngineService = new BusinessRuleEngineService();
        domainId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getRules & saveRule: 복합 비즈니스 룰 등록 및 조회")
    void testSaveAndGetRules() {
        BusinessRuleDto.BusinessRuleItem rule = BusinessRuleDto.BusinessRuleItem.builder()
                .ruleName("해외 거래처 통화코드 필수 검증")
                .conditionExpr("country != 'KR'")
                .validationExpr("currency != null && currency in ['USD', 'EUR', 'JPY']")
                .errorMessage("해외 거래처는 국제 표준 통화코드가 필수입니다.")
                .enabled(true)
                .build();

        BusinessRuleDto.BusinessRuleItem saved = ruleEngineService.saveRule(domainId, rule);
        assertThat(saved.getRuleId()).isNotNull();

        List<BusinessRuleDto.BusinessRuleItem> rules = ruleEngineService.getRules(domainId);
        assertThat(rules).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("evaluateRules: 비즈니스 룰 평가 및 위반 레코드 탐지")
    void testEvaluateRules() {
        List<BusinessRuleDto.RuleEvaluationResult> results = ruleEngineService.evaluateRules(domainId);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getViolationCount()).isGreaterThan(0);
        assertThat(results.get(0).getSampleViolations().get(0).getRecordCode()).startsWith("REC-");
    }
}
