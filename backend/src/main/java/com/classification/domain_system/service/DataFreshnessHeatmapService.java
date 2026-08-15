package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataFreshnessHeatmapDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataFreshnessHeatmapService {

    public DataFreshnessHeatmapDto.FreshnessHeatmapResponse getFreshnessHeatmap() {
        List<DataFreshnessHeatmapDto.DomainFreshnessItem> items = new ArrayList<>();

        items.add(DataFreshnessHeatmapDto.DomainFreshnessItem.builder()
                .domainCode("DOM-CUST")
                .domainName("고객 (Customer Master)")
                .lastUpdatedTime("방금 전 (3분 전)")
                .freshnessSlaMinutes(10)
                .delayMinutes(3)
                .freshnessScore(99)
                .status("FRESH")
                .build());

        items.add(DataFreshnessHeatmapDto.DomainFreshnessItem.builder()
                .domainCode("DOM-PROD")
                .domainName("제품·자재 (Product Master)")
                .lastUpdatedTime("15분 전")
                .freshnessSlaMinutes(30)
                .delayMinutes(15)
                .freshnessScore(95)
                .status("FRESH")
                .build());

        items.add(DataFreshnessHeatmapDto.DomainFreshnessItem.builder()
                .domainCode("DOM-ORDER")
                .domainName("주문·거래 (Order Master)")
                .lastUpdatedTime("실시간 (1분 전)")
                .freshnessSlaMinutes(5)
                .delayMinutes(1)
                .freshnessScore(100)
                .status("FRESH")
                .build());

        items.add(DataFreshnessHeatmapDto.DomainFreshnessItem.builder()
                .domainCode("DOM-ORG")
                .domainName("조직·인사 (Organization Master)")
                .lastUpdatedTime("45분 전")
                .freshnessSlaMinutes(60)
                .delayMinutes(45)
                .freshnessScore(92)
                .status("FRESH")
                .build());

        items.add(DataFreshnessHeatmapDto.DomainFreshnessItem.builder()
                .domainCode("DOM-SUPP")
                .domainName("협력사·공급망 (Supplier Master)")
                .lastUpdatedTime("2시간 전")
                .freshnessSlaMinutes(180)
                .delayMinutes(120)
                .freshnessScore(88)
                .status("FRESH")
                .build());

        double avgScore = items.stream().mapToInt(DataFreshnessHeatmapDto.DomainFreshnessItem::getFreshnessScore).average().orElse(100.0);
        long stale = items.stream().filter(i -> "STALE".equals(i.getStatus())).count();

        return DataFreshnessHeatmapDto.FreshnessHeatmapResponse.builder()
                .overallFreshnessScore((int) Math.round(avgScore))
                .totalDomains(items.size())
                .staleCount((int) stale)
                .domains(items)
                .summary(String.format("전사 5개 핵심 도메인의 데이터 신선도가 종합 %d점으로 실시간 초신선(Fresh) 상태를 유지하고 있습니다.", (int) Math.round(avgScore)))
                .build();
    }
}
