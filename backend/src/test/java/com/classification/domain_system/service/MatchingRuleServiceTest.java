package com.classification.domain_system.service;

import com.classification.domain_system.entity.MatchingRule;
import com.classification.domain_system.repository.MatchingRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchingRuleServiceTest {

    @Mock
    private MatchingRuleRepository matchingRuleRepository;

    @InjectMocks
    private MatchingRuleServiceImpl matchingRuleService;

    private UUID domainId;
    private UUID ruleId;
    private MatchingRule rule;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        ruleId = UUID.randomUUID();

        rule = new MatchingRule();
        rule.setId(ruleId);
        rule.setDomainId(domainId);
        rule.setRuleName("이메일 일치 검사");
        rule.setMatchType("EXACT");
        rule.setActive(true);
    }

    @Test
    @DisplayName("도메인별 매칭 룰 목록 조회 성공")
    void getRulesByDomainId_success() {
        given(matchingRuleRepository.findByDomainId(domainId)).willReturn(List.of(rule));

        List<MatchingRule> rules = matchingRuleService.getRulesByDomainId(domainId);

        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).getRuleName()).isEqualTo("이메일 일치 검사");
    }

    @Test
    @DisplayName("매칭 룰 생성 성공")
    void createRule_success() {
        given(matchingRuleRepository.save(any(MatchingRule.class))).willReturn(rule);

        MatchingRule created = matchingRuleService.createRule(domainId, rule);

        assertThat(created).isNotNull();
        assertThat(created.getDomainId()).isEqualTo(domainId);
        verify(matchingRuleRepository).save(rule);
    }

    @Test
    @DisplayName("매칭 룰 수정 성공")
    void updateRule_success() {
        MatchingRule updateReq = new MatchingRule();
        updateReq.setRuleName("수정된 룰");
        updateReq.setMatchType("FUZZY");
        updateReq.setActive(false);

        given(matchingRuleRepository.findById(ruleId)).willReturn(Optional.of(rule));
        given(matchingRuleRepository.save(any(MatchingRule.class))).willReturn(rule);

        MatchingRule updated = matchingRuleService.updateRule(domainId, ruleId, updateReq);

        assertThat(updated).isNotNull();
        assertThat(rule.getRuleName()).isEqualTo("수정된 룰");
        assertThat(rule.getMatchType()).isEqualTo("FUZZY");
        assertThat(rule.isActive()).isFalse();
    }

    @Test
    @DisplayName("매칭 룰 삭제 성공")
    void deleteRule_success() {
        matchingRuleService.deleteRule(domainId, ruleId);

        verify(matchingRuleRepository).deleteById(ruleId);
    }
}
