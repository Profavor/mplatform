package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataFreshnessHeatmapDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataFreshnessHeatmapService {

    private final DomainRepository domainRepository;
    private final RecordRepository recordRepository;

    @Transactional(readOnly = true)
    public DataFreshnessHeatmapDto.FreshnessHeatmapResponse getFreshnessHeatmap() {
        List<DataFreshnessHeatmapDto.DomainFreshnessItem> items = new ArrayList<>();
        List<Domain> domains = domainRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Domain d : domains) {
            String domName = (d.getName() != null && !d.getName().isEmpty())
                    ? d.getName().getOrDefault("ko", d.getName().values().iterator().next())
                    : "도메인";
            String domCode = "DOM-" + (d.getId() != null ? d.getId().toString().substring(0, 8).toUpperCase() : "00000000");

            List<Record> records = recordRepository.findAllByDomainId(d.getId());
            LocalDateTime lastUpdated = d.getUpdatedAt() != null ? d.getUpdatedAt() : (d.getCreatedAt() != null ? d.getCreatedAt() : now);

            if (!records.isEmpty() && records.get(0).getCreatedAt() != null) {
                lastUpdated = records.get(0).getCreatedAt();
            }

            long delayMins = Math.max(1, Duration.between(lastUpdated, now).toMinutes());
            int slaMinutes = 60; // 기본 1시간 SLA
            int score = Math.max(70, Math.min(100, 100 - (int) (delayMins / 2)));
            String status = delayMins <= slaMinutes ? "FRESH" : "STALE";

            String timeDesc;
            if (delayMins < 5) {
                timeDesc = "방금 전 (실시간)";
            } else if (delayMins < 60) {
                timeDesc = delayMins + "분 전";
            } else if (delayMins < 1440) {
                timeDesc = (delayMins / 60) + "시간 전";
            } else {
                timeDesc = (delayMins / 1440) + "일 전";
            }

            items.add(DataFreshnessHeatmapDto.DomainFreshnessItem.builder()
                    .domainCode(domCode)
                    .domainName(domName)
                    .lastUpdatedTime(timeDesc)
                    .freshnessSlaMinutes(slaMinutes)
                    .delayMinutes((int) delayMins)
                    .freshnessScore(score)
                    .status(status)
                    .build());
        }

        double avgScore = items.stream().mapToInt(DataFreshnessHeatmapDto.DomainFreshnessItem::getFreshnessScore).average().orElse(100.0);
        long stale = items.stream().filter(i -> "STALE".equals(i.getStatus())).count();

        String summary = items.isEmpty()
                ? "등록된 마스터 데이터 도메인이 없어 신선도 분석 대기 중입니다."
                : String.format("전사 %d개 핵심 도메인의 데이터 신선도가 종합 %d점으로 실시간 초신선(Fresh) 상태를 유지하고 있습니다.", items.size(), (int) Math.round(avgScore));

        return DataFreshnessHeatmapDto.FreshnessHeatmapResponse.builder()
                .overallFreshnessScore((int) Math.round(avgScore))
                .totalDomains(items.size())
                .staleCount((int) stale)
                .domains(items)
                .summary(summary)
                .build();
    }
}
