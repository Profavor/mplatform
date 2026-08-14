package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MatchFeedbackStats;
import com.classification.domain_system.entity.MatchingRule;
import com.classification.domain_system.service.MatchFeedbackService;
import com.classification.domain_system.service.MatchingRuleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingRuleControllerTest {

    @Mock
    private MatchingRuleService matchingRuleService;

    @Mock
    private MatchFeedbackService matchFeedbackService;

    @InjectMocks
    private MatchingRuleController matchingRuleController;

    private MatchingRule createMockRule(UUID domainId, String name) {
        MatchingRule rule = new MatchingRule();
        rule.setId(UUID.randomUUID());
        rule.setDomainId(domainId);
        rule.setRuleName(name);
        rule.setTargetFieldKeys("[\"email\"]");
        rule.setMatchType("EXACT");
        rule.setSimilarityThreshold(1.0);
        rule.setActive(true);
        return rule;
    }

    @Test
    @DisplayName("도메인별 매칭 룰 목록 조회 성공")
    void testGetRules_Success() {
        UUID domainId = UUID.randomUUID();
        MatchingRule rule = createMockRule(domainId, "이메일 완전 일치");
        when(matchingRuleService.getRulesByDomainId(domainId)).thenReturn(List.of(rule));

        ResponseEntity<List<MatchingRule>> response = matchingRuleController.getRules(domainId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("이메일 완전 일치", response.getBody().get(0).getRuleName());
    }

    @Test
    @DisplayName("매칭 룰 생성 성공")
    void testCreateRule_Success() {
        UUID domainId = UUID.randomUUID();
        MatchingRule input = new MatchingRule();
        input.setRuleName("새 매칭 룰");

        MatchingRule created = createMockRule(domainId, "새 매칭 룰");
        when(matchingRuleService.createRule(eq(domainId), any(MatchingRule.class))).thenReturn(created);

        ResponseEntity<MatchingRule> response = matchingRuleController.createRule(domainId, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("새 매칭 룰", response.getBody().getRuleName());
    }

    @Test
    @DisplayName("매칭 룰 수정 성공")
    void testUpdateRule_Success() {
        UUID domainId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();
        MatchingRule input = new MatchingRule();
        input.setRuleName("수정된 룰");

        MatchingRule updated = createMockRule(domainId, "수정된 룰");
        updated.setId(ruleId);
        when(matchingRuleService.updateRule(eq(domainId), eq(ruleId), any(MatchingRule.class))).thenReturn(updated);

        ResponseEntity<MatchingRule> response = matchingRuleController.updateRule(domainId, ruleId, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("수정된 룰", response.getBody().getRuleName());
    }

    @Test
    @DisplayName("매칭 룰 삭제 성공")
    void testDeleteRule_Success() {
        UUID domainId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        ResponseEntity<Void> response = matchingRuleController.deleteRule(domainId, ruleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(matchingRuleService).deleteRule(domainId, ruleId);
    }

    @Test
    @DisplayName("도메인 매칭 룰 피드백 통계 요약 조회 성공")
    void testGetFeedbackSummary_Success() {
        UUID domainId = UUID.randomUUID();
        MatchFeedbackStats stats = MatchFeedbackStats.builder()
                .ruleId(UUID.randomUUID())
                .ruleName("테스트 룰")
                .matchType("EXACT")
                .currentThreshold(1.0)
                .build();
        when(matchFeedbackService.getFeedbackSummary(domainId)).thenReturn(List.of(stats));

        ResponseEntity<List<MatchFeedbackStats>> response = matchingRuleController.getFeedbackSummary(domainId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("특정 매칭 룰 상세 피드백 통계 조회 성공")
    void testGetFeedback_Success() {
        UUID domainId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();
        MatchFeedbackStats stats = MatchFeedbackStats.builder()
                .ruleId(ruleId)
                .ruleName("테스트 룰")
                .matchType("FUZZY")
                .currentThreshold(0.85)
                .build();
        when(matchFeedbackService.getFeedbackStats(ruleId)).thenReturn(stats);

        ResponseEntity<MatchFeedbackStats> response = matchingRuleController.getFeedback(domainId, ruleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ruleId, response.getBody().getRuleId());
    }
}
