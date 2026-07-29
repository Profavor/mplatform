package com.classification.domain_system.service;

import com.classification.domain_system.dto.MatchFeedbackStats;
import com.classification.domain_system.entity.MatchingRule;
import com.classification.domain_system.repository.MatchCandidateRepository;
import com.classification.domain_system.repository.MatchingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 매칭 후보 검토 결과를 집계하여 MatchingRule.similarityThreshold 튜닝 지표를 제공하는 서비스.
 *
 * <p>스튜어드가 검토 완료한 MatchCandidate(CONFIRMED_MERGE / REJECTED) 데이터를
 * 룰별로 집계하여 정탐률, 점수 분포, 추천 임계값을 산출합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class MatchFeedbackService {

    private static final String STATUS_CONFIRMED = "CONFIRMED_MERGE";
    private static final String STATUS_REJECTED = "REJECTED";

    private final MatchCandidateRepository matchCandidateRepository;
    private final MatchingRuleRepository matchingRuleRepository;

    /**
     * 특정 매칭 룰의 피드백 통계를 조회합니다.
     *
     * @param ruleId 매칭 룰 ID
     * @return 피드백 통계 DTO
     * @throws RuntimeException 룰이 존재하지 않는 경우
     */
    public MatchFeedbackStats getFeedbackStats(UUID ruleId) {
        MatchingRule rule = matchingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Matching rule not found: " + ruleId));
        return buildStats(rule);
    }

    /**
     * 도메인 내 모든 활성 매칭 룰의 피드백 통계를 일괄 조회합니다.
     *
     * @param domainId 도메인 ID
     * @return 각 룰별 피드백 통계 리스트
     */
    public List<MatchFeedbackStats> getFeedbackSummary(UUID domainId) {
        List<MatchingRule> rules = matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId);
        return rules.stream()
                .map(this::buildStats)
                .collect(Collectors.toList());
    }

    /**
     * 단일 룰에 대한 피드백 통계를 산출합니다.
     */
    MatchFeedbackStats buildStats(MatchingRule rule) {
        UUID ruleId = rule.getId();

        long confirmedCount = matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, STATUS_CONFIRMED);
        long rejectedCount = matchCandidateRepository.countByMatchedRuleIdAndStatus(ruleId, STATUS_REJECTED);
        long totalReviewed = confirmedCount + rejectedCount;

        Double confirmedAvgScore = matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, STATUS_CONFIRMED);
        Double rejectedAvgScore = matchCandidateRepository.findAvgScoreByMatchedRuleIdAndStatus(ruleId, STATUS_REJECTED);
        Double rejectedMaxScore = matchCandidateRepository.findMaxScoreByMatchedRuleIdAndStatus(ruleId, STATUS_REJECTED);
        Double confirmedMinScore = matchCandidateRepository.findMinScoreByMatchedRuleIdAndStatus(ruleId, STATUS_CONFIRMED);

        double precision = totalReviewed > 0 ? (double) confirmedCount / totalReviewed : 0.0;
        Double recommendedThreshold = calculateRecommendedThreshold(rejectedMaxScore, confirmedMinScore, rule.getSimilarityThreshold());
        String recommendation = generateRecommendation(rule, precision, rejectedMaxScore, confirmedMinScore, recommendedThreshold, totalReviewed);

        return MatchFeedbackStats.builder()
                .ruleId(ruleId)
                .ruleName(rule.getRuleName())
                .matchType(rule.getMatchType())
                .currentThreshold(rule.getSimilarityThreshold() != null ? rule.getSimilarityThreshold() : 0.85)
                .totalReviewed(totalReviewed)
                .confirmedCount(confirmedCount)
                .rejectedCount(rejectedCount)
                .confirmedAvgScore(confirmedAvgScore)
                .rejectedAvgScore(rejectedAvgScore)
                .rejectedMaxScore(rejectedMaxScore)
                .confirmedMinScore(confirmedMinScore)
                .precision(precision)
                .recommendedThreshold(recommendedThreshold)
                .recommendation(recommendation)
                .build();
    }

    /**
     * 추천 임계값을 계산합니다.
     *
     * <p>오탐 최대 점수와 정탐 최소 점수의 중간값을 추천합니다.
     * 두 값이 겹치면(오탐 최대 > 정탐 최소) 정탐 최소 점수를 추천합니다.</p>
     *
     * @return 추천 임계값 (데이터 부족 시 null)
     */
    Double calculateRecommendedThreshold(Double rejectedMaxScore, Double confirmedMinScore, Double currentThreshold) {
        if (rejectedMaxScore == null && confirmedMinScore == null) {
            return null; // 검토 데이터 부족
        }

        if (rejectedMaxScore != null && confirmedMinScore != null) {
            if (rejectedMaxScore >= confirmedMinScore) {
                // 오탐과 정탐 점수가 겹침 → 정탐 최소값을 추천 (정탐 보전 우선)
                return confirmedMinScore;
            }
            // 정상 분리: 중간값 추천
            return (rejectedMaxScore + confirmedMinScore) / 2.0;
        }

        if (rejectedMaxScore != null) {
            // 오탐만 존재 → 오탐 최대 점수 바로 위로 추천
            return Math.min(rejectedMaxScore + 0.02, 1.0);
        }

        // 정탐만 존재 → 현재 threshold 유지
        return currentThreshold;
    }

    /**
     * 피드백 통계 기반 텍스트 권고사항을 생성합니다.
     */
    String generateRecommendation(MatchingRule rule, double precision, Double rejectedMaxScore,
                                  Double confirmedMinScore, Double recommendedThreshold, long totalReviewed) {
        if (totalReviewed == 0) {
            return "검토된 데이터가 없습니다. 스튜어드의 매칭 후보 검토가 필요합니다.";
        }

        if (totalReviewed < 5) {
            return String.format("검토 건수가 %d건으로 적습니다. 충분한 데이터(최소 5건 이상) 확보 후 임계값 조정을 권장합니다.", totalReviewed);
        }

        double currentThreshold = rule.getSimilarityThreshold() != null ? rule.getSimilarityThreshold() : 0.85;

        if (precision >= 0.95) {
            return String.format("정탐률 %.1f%%로 우수합니다. 현재 임계값(%.2f)을 유지해도 좋습니다.", precision * 100, currentThreshold);
        }

        if (recommendedThreshold != null && recommendedThreshold > currentThreshold) {
            return String.format("정탐률 %.1f%%. 현재 임계값(%.2f)을 %.2f로 올리면 오탐을 줄일 수 있습니다.",
                    precision * 100, currentThreshold, recommendedThreshold);
        }

        return String.format("정탐률 %.1f%%. 매칭 대상 필드 조합 변경도 함께 검토해 보세요.", precision * 100);
    }
}
