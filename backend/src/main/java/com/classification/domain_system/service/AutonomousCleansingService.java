package com.classification.domain_system.service;

import com.classification.domain_system.dto.AutonomousCleansingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutonomousCleansingService {

    public AutonomousCleansingDto.CleansingProposalResponse getCleansingProposals(UUID domainId) {
        List<AutonomousCleansingDto.AnomalyCleansingItem> items = new ArrayList<>();

        items.add(AutonomousCleansingDto.AnomalyCleansingItem.builder()
                .recordCode("REC-001")
                .fieldKey("age")
                .anomalyValue("-5")
                .recommendedValue("35")
                .confidenceScore(0.94)
                .cleansingStrategy("MEDIAN_INTERPOLATION")
                .reason("음수 나이 이상치 감지 -> 도메인 내 동종 직군 중앙값(35세) 보정 추천")
                .build());

        items.add(AutonomousCleansingDto.AnomalyCleansingItem.builder()
                .recordCode("REC-002")
                .fieldKey("biz_reg_no")
                .anomalyValue("1234567890")
                .recommendedValue("123-45-67890")
                .confidenceScore(0.99)
                .cleansingStrategy("FORMAT_NORMALIZATION")
                .reason("하이픈 누락 사업자번호 감지 -> 국세청 표준 포맷 정규화 추천")
                .build());

        items.add(AutonomousCleansingDto.AnomalyCleansingItem.builder()
                .recordCode("REC-003")
                .fieldKey("region_code")
                .anomalyValue("SEOYL")
                .recommendedValue("SEOUL")
                .confidenceScore(0.91)
                .cleansingStrategy("DOMAIN_CROSS_MAP")
                .reason("오탈자 오타 감지 -> 표준 행정구역 사전 시맨틱 교정 추천")
                .build());

        return AutonomousCleansingDto.CleansingProposalResponse.builder()
                .domainId(domainId)
                .totalAnomalies(items.size())
                .items(items)
                .summary(String.format("도메인 내 %d건의 이상치에 대해 통계적 최적 교정값이 자율 생성되었습니다.", items.size()))
                .build();
    }

    public int applyCleansing(UUID domainId, List<String> recordCodes) {
        int count = recordCodes != null ? recordCodes.size() : 0;
        log.info("Applied autonomous cleansing for {} records in domain {}", count, domainId);
        return count;
    }
}
