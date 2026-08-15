package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataAssetDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataAssetValuationService {

    private final DomainRepository domainRepository;
    private final RecordRepository recordRepository;

    @Transactional(readOnly = true)
    public DataAssetDto.DataAssetSummaryResponse evaluateDataAssets() {
        List<Domain> domains = domainRepository.findAll();
        List<DataAssetDto.DomainValuationItem> items = new ArrayList<>();

        long totalValueWon = 0;
        double sumQuality = 0;

        for (Domain d : domains) {
            int recordCount = recordRepository.findAllByDomainId(d.getId()).size();
            int usageScore = Math.min(100, Math.max(50, recordCount * 5));
            double dqScore = 96.5; // Benchmark standard
            int channels = 3;

            long calculatedValue = (recordCount * 50_000L) + ((long) (dqScore * 1_000_000L)) + (channels * 10_000_000L);
            String rating = "BBB";
            if (calculatedValue >= 150_000_000L) rating = "AAA";
            else if (calculatedValue >= 100_000_000L) rating = "AA";
            else if (calculatedValue >= 50_000_000L) rating = "A";

            totalValueWon += calculatedValue;
            sumQuality += dqScore;

            String domainNameStr = "도메인";
            if (d.getName() != null && !d.getName().isEmpty()) {
                domainNameStr = d.getName().getOrDefault("ko", d.getName().getOrDefault("en", "도메인"));
            }

            items.add(DataAssetDto.DomainValuationItem.builder()
                    .domainId(d.getId())
                    .domainName(domainNameStr)
                    .recordCount(recordCount)
                    .usageFrequencyScore(usageScore)
                    .dqQualityScore(dqScore)
                    .connectedChannelCount(channels)
                    .assetRating(rating)
                    .estimatedAssetValueWon(calculatedValue)
                    .build());
        }

        double avgQuality = domains.isEmpty() ? 0 : Math.round((sumQuality / domains.size()) * 10.0) / 10.0;

        return DataAssetDto.DataAssetSummaryResponse.builder()
                .totalDomainsEvaluated(domains.size())
                .totalEstimatedAssetValueWon(totalValueWon)
                .averageQualityScore(avgQuality)
                .domainValuations(items)
                .summary(String.format("총 %d개 도메인의 데이터 자산 평가가 완료되었으며, 전사 데이터 자산 총액은 약 %,d원입니다.", domains.size(), totalValueWon))
                .build();
    }
}
