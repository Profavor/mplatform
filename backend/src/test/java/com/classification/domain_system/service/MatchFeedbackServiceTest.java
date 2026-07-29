package com.classification.domain_system.service;

import com.classification.domain_system.dto.MatchFeedbackStats;
import com.classification.domain_system.entity.MatchingRule;
import com.classification.domain_system.repository.MatchCandidateRepository;
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
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchFeedbackServiceTest {

    @Mock
    private MatchCandidateRepository matchCandidateRepository;

    @Mock
    private MatchingRuleRepository matchingRuleRepository;

    @InjectMocks
    private MatchFeedbackService matchFeedbackService;

    private MatchingRule fuzzyRule;
    private UUID ruleId;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        ruleId = UUID.randomUUID();
        domainId = UUID.randomUUID();

        fuzzyRule = new MatchingRule();
        fuzzyRule.setId(ruleId);
        fuzzyRule.setDomainId(domainId);
        fuzzyRule.setRuleName("이메일+전화 퍼지 매칭");
        fuzzyRule.setMatchType("FUZZY");
        fuzzyRule.setSimilarityThreshold(0.85);
        fuzzyRule.setActive(true);
    }

    @Test
    @DisplayName("정탐/오탐 혼합 시 정탐률과 추천 임계값이 올바르게 계산된다")
    void getFeedbackStats_MixedReviews_CalculatesCorrectly() {
        when(matchingRuleRepository.findById(ruleId)).thenReturn(Optional.of(fuzzyRule));
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(7L);
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(3L);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(0.95);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(0.88);
        when(matchCandidateRepository.findMaxScoreByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(0.91);
        when(matchCandidateRepository.findMinScoreByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(0.93);

        MatchFeedbackStats stats = matchFeedbackService.getFeedbackStats(ruleId);

        assertThat(stats.getRuleId()).isEqualTo(ruleId);
        assertThat(stats.getTotalReviewed()).isEqualTo(10);
        assertThat(stats.getConfirmedCount()).isEqualTo(7);
        assertThat(stats.getRejectedCount()).isEqualTo(3);
        assertThat(stats.getPrecision()).isCloseTo(0.7, within(0.001));
        // 추천 임계값 = (0.91 + 0.93) / 2 = 0.92
        assertThat(stats.getRecommendedThreshold()).isCloseTo(0.92, within(0.001));
        assertThat(stats.getRecommendation()).contains("0.92");
    }

    @Test
    @DisplayName("검토 데이터가 없을 때 안전한 기본값을 반환한다")
    void getFeedbackStats_NoReviews_ReturnsSafeDefaults() {
        when(matchingRuleRepository.findById(ruleId)).thenReturn(Optional.of(fuzzyRule));
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(0L);
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(0L);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(null);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(null);
        when(matchCandidateRepository.findMaxScoreByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(null);
        when(matchCandidateRepository.findMinScoreByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(null);

        MatchFeedbackStats stats = matchFeedbackService.getFeedbackStats(ruleId);

        assertThat(stats.getTotalReviewed()).isEqualTo(0);
        assertThat(stats.getPrecision()).isEqualTo(0.0);
        assertThat(stats.getRecommendedThreshold()).isNull();
        assertThat(stats.getRecommendation()).contains("검토된 데이터가 없습니다");
    }

    @Test
    @DisplayName("오탐만 있는 경우 오탐 최대 점수 + 0.02를 추천 임계값으로 제시한다")
    void getFeedbackStats_OnlyRejections_RecommendsAboveMaxRejected() {
        when(matchingRuleRepository.findById(ruleId)).thenReturn(Optional.of(fuzzyRule));
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(0L);
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(6L);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(null);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(0.87);
        when(matchCandidateRepository.findMaxScoreByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(0.90);
        when(matchCandidateRepository.findMinScoreByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(null);

        MatchFeedbackStats stats = matchFeedbackService.getFeedbackStats(ruleId);

        assertThat(stats.getPrecision()).isEqualTo(0.0);
        // 오탐만: 0.90 + 0.02 = 0.92
        assertThat(stats.getRecommendedThreshold()).isCloseTo(0.92, within(0.001));
    }

    @Test
    @DisplayName("정탐만 있는 경우 현재 임계값을 유지 추천한다")
    void getFeedbackStats_OnlyConfirmations_KeepsCurrentThreshold() {
        when(matchingRuleRepository.findById(ruleId)).thenReturn(Optional.of(fuzzyRule));
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(8L);
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(0L);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(0.96);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(null);
        when(matchCandidateRepository.findMaxScoreByMatchedRuleIdAndStatus(ruleId, "REJECTED")).thenReturn(null);
        when(matchCandidateRepository.findMinScoreByMatchedRuleIdAndStatus(ruleId, "CONFIRMED_MERGE")).thenReturn(0.92);

        MatchFeedbackStats stats = matchFeedbackService.getFeedbackStats(ruleId);

        assertThat(stats.getPrecision()).isEqualTo(1.0);
        assertThat(stats.getRecommendedThreshold()).isEqualTo(0.85); // 현재 threshold 유지
        assertThat(stats.getRecommendation()).contains("우수");
    }

    @Test
    @DisplayName("도메인 피드백 요약은 활성 룰만 포함한다")
    void getFeedbackSummary_ReturnsOnlyActiveRules() {
        MatchingRule rule2 = new MatchingRule();
        rule2.setId(UUID.randomUUID());
        rule2.setDomainId(domainId);
        rule2.setRuleName("이름 정확 매칭");
        rule2.setMatchType("EXACT");
        rule2.setSimilarityThreshold(1.0);
        rule2.setActive(true);

        when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of(fuzzyRule, rule2));

        // fuzzyRule 통계
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(fuzzyRule.getId(), "CONFIRMED_MERGE")).thenReturn(5L);
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(fuzzyRule.getId(), "REJECTED")).thenReturn(2L);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(fuzzyRule.getId(), "CONFIRMED_MERGE")).thenReturn(0.94);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(fuzzyRule.getId(), "REJECTED")).thenReturn(0.87);
        when(matchCandidateRepository.findMaxScoreByMatchedRuleIdAndStatus(fuzzyRule.getId(), "REJECTED")).thenReturn(0.89);
        when(matchCandidateRepository.findMinScoreByMatchedRuleIdAndStatus(fuzzyRule.getId(), "CONFIRMED_MERGE")).thenReturn(0.91);

        // rule2 통계
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(rule2.getId(), "CONFIRMED_MERGE")).thenReturn(0L);
        when(matchCandidateRepository.countByMatchedRuleIdAndStatus(rule2.getId(), "REJECTED")).thenReturn(0L);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(rule2.getId(), "CONFIRMED_MERGE")).thenReturn(null);
        when(matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(rule2.getId(), "REJECTED")).thenReturn(null);
        when(matchCandidateRepository.findMaxScoreByMatchedRuleIdAndStatus(rule2.getId(), "REJECTED")).thenReturn(null);
        when(matchCandidateRepository.findMinScoreByMatchedRuleIdAndStatus(rule2.getId(), "CONFIRMED_MERGE")).thenReturn(null);

        List<MatchFeedbackStats> summary = matchFeedbackService.getFeedbackSummary(domainId);

        assertThat(summary).hasSize(2);
        assertThat(summary.get(0).getRuleName()).isEqualTo("이메일+전화 퍼지 매칭");
        assertThat(summary.get(0).getTotalReviewed()).isEqualTo(7);
        assertThat(summary.get(1).getRuleName()).isEqualTo("이름 정확 매칭");
        assertThat(summary.get(1).getTotalReviewed()).isEqualTo(0);
    }
}
