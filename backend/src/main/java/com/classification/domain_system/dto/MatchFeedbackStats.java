package com.classification.domain_system.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 매칭 룰별 스튜어드 검토 피드백 통계 DTO.
 * 정탐/오탐 분포를 기반으로 similarityThreshold 튜닝 지표를 제공합니다.
 */
@Getter
@Builder
public class MatchFeedbackStats {
    private final UUID ruleId;
    private final String ruleName;
    private final String matchType;
    private final double currentThreshold;

    /** 전체 검토 건수 (CONFIRMED + REJECTED) */
    private final long totalReviewed;
    /** 정탐(CONFIRMED_MERGE) 건수 */
    private final long confirmedCount;
    /** 오탐(REJECTED) 건수 */
    private final long rejectedCount;

    /** 정탐 평균 점수 (null: 데이터 없음) */
    private final Double confirmedAvgScore;
    /** 오탐 평균 점수 (null: 데이터 없음) */
    private final Double rejectedAvgScore;
    /** 오탐 최대 점수 - 이 값 이상의 threshold가 필요 */
    private final Double rejectedMaxScore;
    /** 정탐 최소 점수 - 이 값 이하면 정탐도 놓칠 수 있음 */
    private final Double confirmedMinScore;

    /** 정탐률: confirmed / (confirmed + rejected), 0.0~1.0 */
    private final double precision;
    /** 추천 임계값 (null: 데이터 부족으로 산출 불가) */
    private final Double recommendedThreshold;
    /** 텍스트 권고사항 */
    private final String recommendation;
}
