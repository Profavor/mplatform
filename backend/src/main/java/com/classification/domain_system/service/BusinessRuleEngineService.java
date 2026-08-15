package com.classification.domain_system.service;

import com.classification.domain_system.dto.BusinessRuleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessRuleEngineService {

    private final Map<UUID, List<BusinessRuleDto.BusinessRuleItem>> domainRules = new ConcurrentHashMap<>();

    public List<BusinessRuleDto.BusinessRuleItem> getRules(UUID domainId) {
        return domainRules.computeIfAbsent(domainId, k -> {
            List<BusinessRuleDto.BusinessRuleItem> defaultRules = new ArrayList<>();
            defaultRules.add(BusinessRuleDto.BusinessRuleItem.builder()
                    .ruleId("BR-001")
                    .ruleName("VIP 고객 필수 사업자번호 및 신용등급 검증")
                    .conditionExpr("grade == 'VIP'")
                    .validationExpr("biz_no != null && rating in ['A', 'B']")
                    .errorMessage("VIP 등급 고객은 유효한 사업자등록번호와 A/B 등급이 필수입니다.")
                    .enabled(true)
                    .build());
            return defaultRules;
        });
    }

    public BusinessRuleDto.BusinessRuleItem saveRule(UUID domainId, BusinessRuleDto.BusinessRuleItem rule) {
        List<BusinessRuleDto.BusinessRuleItem> rules = getRules(domainId);
        String id = rule.getRuleId() != null ? rule.getRuleId() : "BR-" + String.format("%03d", rules.size() + 1);
        rule.setRuleId(id);
        rules.add(rule);
        return rule;
    }

    public List<BusinessRuleDto.RuleEvaluationResult> evaluateRules(UUID domainId) {
        List<BusinessRuleDto.BusinessRuleItem> rules = getRules(domainId);
        List<BusinessRuleDto.RuleEvaluationResult> results = new ArrayList<>();

        for (BusinessRuleDto.BusinessRuleItem r : rules) {
            if (!r.isEnabled()) continue;

            List<BusinessRuleDto.ViolationItem> violations = new ArrayList<>();
            violations.add(BusinessRuleDto.ViolationItem.builder()
                    .recordCode("REC-002")
                    .reason("grade='VIP'이나 biz_no가 누락되어 검증식을 만족하지 않음")
                    .build());

            results.add(BusinessRuleDto.RuleEvaluationResult.builder()
                    .ruleId(r.getRuleId())
                    .ruleName(r.getRuleName())
                    .passed(violations.isEmpty())
                    .violationCount(violations.size())
                    .sampleViolations(violations)
                    .build());
        }

        return results;
    }
}
