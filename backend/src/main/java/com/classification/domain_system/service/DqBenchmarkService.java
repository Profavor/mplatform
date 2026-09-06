package com.classification.domain_system.service;

import com.classification.domain_system.dto.DqBenchmarkDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.DqScoreSnapshot;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.DqRuleRepository;
import com.classification.domain_system.repository.DqScoreSnapshotRepository;
import com.classification.domain_system.service.dq.DqRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DqBenchmarkService {

    private final DomainRepository domainRepository;
    private final DqRuleEngine dqRuleEngine;
    private final DqRuleRepository dqRuleRepository;
    private final DqScoreSnapshotRepository dqScoreSnapshotRepository;

    @Transactional(readOnly = true)
    public DqBenchmarkDto.OverviewResponse getBenchmarkOverview() {
        List<Domain> domains = domainRepository.findAll();
        if (domains.isEmpty()) {
            return DqBenchmarkDto.OverviewResponse.builder()
                    .averageScore(0.0)
                    .totalMonitoredDomains(0)
                    .highestDomain(null)
                    .lowestDomain(null)
                    .totalViolations(0L)
                    .highRiskDomainCount(0)
                    .items(Collections.emptyList())
                    .build();
        }

        List<DqBenchmarkDto.BenchmarkItem> items = new ArrayList<>();
        long grandTotalViolations = 0L;
        double sumScore = 0.0;
        int highRiskCount = 0;

        for (Domain domain : domains) {
            UUID domainId = domain.getId();
            Map<String, Object> scoreData = dqRuleEngine.getDomainDqScore(domainId);
            long ruleCount = dqRuleRepository.countByDomainId(domainId);

            double score = getDoubleValue(scoreData.get("score"), 100.0);
            long totalRecords = getLongValue(scoreData.get("totalRecords"), 0L);
            long totalViolations = getLongValue(scoreData.get("totalViolations"), 0L);

            Map<String, Object> severityMap = Collections.emptyMap();
            Object rawSeverity = scoreData.get("violationsBySeverity");
            if (rawSeverity instanceof Map<?, ?> m) {
                //noinspection unchecked
                severityMap = (Map<String, Object>) m;
            }

            long errorCount = getLongValue(severityMap.get("ERROR"), 0L);
            long warningCount = getLongValue(severityMap.get("WARNING"), 0L);

            String grade = calculateGrade(score);
            String riskLevel = calculateRiskLevel(score);
            if ("HIGH_RISK".equals(riskLevel)) {
                highRiskCount++;
            }

            double trendDelta = 0.0;
            List<DqScoreSnapshot> recentSnapshots = dqScoreSnapshotRepository.findTop30ByDomainIdOrderByRecordedAtDesc(domainId);
            if (recentSnapshots != null && recentSnapshots.size() >= 2) {
                trendDelta = recentSnapshots.get(0).getScore() - recentSnapshots.get(1).getScore();
            }

            Map<String, Double> dimensionScores = extractDimensionScores(scoreData.get("dimensionScores"));

            DqBenchmarkDto.BenchmarkItem item = DqBenchmarkDto.BenchmarkItem.builder()
                    .domainId(domainId)
                    .domainCode(resolveDomainCode(domain))
                    .domainName(domain.getName())
                    .score(score)
                    .grade(grade)
                    .totalRecords(totalRecords)
                    .totalViolations(totalViolations)
                    .errorCount(errorCount)
                    .warningCount(warningCount)
                    .ruleCount(ruleCount)
                    .riskLevel(riskLevel)
                    .trendDelta(Math.round(trendDelta * 100.0) / 100.0)
                    .dimensionScores(dimensionScores)
                    .build();

            items.add(item);
            grandTotalViolations += totalViolations;
            sumScore += score;
        }

        // Sort items by score desc
        items.sort(Comparator.comparingDouble(DqBenchmarkDto.BenchmarkItem::getScore).reversed());

        DqBenchmarkDto.BenchmarkItem highest = items.stream()
                .max(Comparator.comparingDouble(DqBenchmarkDto.BenchmarkItem::getScore))
                .orElse(null);

        DqBenchmarkDto.BenchmarkItem lowest = items.stream()
                .min(Comparator.comparingDouble(DqBenchmarkDto.BenchmarkItem::getScore))
                .orElse(null);

        double averageScore = Math.round((sumScore / domains.size()) * 100.0) / 100.0;

        return DqBenchmarkDto.OverviewResponse.builder()
                .averageScore(averageScore)
                .totalMonitoredDomains(domains.size())
                .highestDomain(highest)
                .lowestDomain(lowest)
                .totalViolations(grandTotalViolations)
                .highRiskDomainCount(highRiskCount)
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DqBenchmarkDto.MultiDomainTrend> getMultiDomainTrend(int days) {
        if (days <= 0) {
            days = 30;
        }
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(days);

        List<Domain> domains = domainRepository.findAll();
        List<DqBenchmarkDto.MultiDomainTrend> result = new ArrayList<>();

        for (Domain domain : domains) {
            List<DqScoreSnapshot> snapshots = dqScoreSnapshotRepository
                    .findByDomainIdAndRecordedAtBetweenOrderByRecordedAtAsc(domain.getId(), from, to);

            List<DqBenchmarkDto.TrendPoint> dataPoints = (snapshots == null ? Collections.<DqScoreSnapshot>emptyList() : snapshots)
                    .stream()
                    .map(s -> DqBenchmarkDto.TrendPoint.builder()
                            .recordedAt(s.getRecordedAt())
                            .score(s.getScore())
                            .totalViolations(s.getTotalViolations())
                            .build())
                    .collect(Collectors.toList());

            result.add(DqBenchmarkDto.MultiDomainTrend.builder()
                    .domainId(domain.getId())
                    .domainCode(resolveDomainCode(domain))
                    .domainName(domain.getName())
                    .dataPoints(dataPoints)
                    .build());
        }

        return result;
    }

    private String resolveDomainCode(Domain domain) {
        if (domain == null) {
            return "DOM-UNKNOWN";
        }
        if (domain.getNumberingPattern() != null && !domain.getNumberingPattern().isBlank()) {
            return domain.getNumberingPattern();
        }
        if (domain.getId() != null) {
            return "DOM-" + domain.getId().toString().substring(0, 8).toUpperCase();
        }
        return "DOM-UNKNOWN";
    }

    private String calculateGrade(double score) {
        if (score >= 90.0) return "A";
        if (score >= 80.0) return "B";
        if (score >= 70.0) return "C";
        return "D";
    }

    private String calculateRiskLevel(double score) {
        if (score >= 90.0) return "HEALTHY";
        if (score >= 80.0) return "WARNING";
        return "HIGH_RISK";
    }

    private double getDoubleValue(Object val, double fallback) {
        if (val instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }

    private long getLongValue(Object val, long fallback) {
        if (val instanceof Number n) {
            return n.longValue();
        }
        return fallback;
    }

    private Map<String, Double> extractDimensionScores(Object raw) {
        if (raw instanceof Map<?, ?> m) {
            Map<String, Double> res = new HashMap<>();
            for (Map.Entry<?, ?> entry : m.entrySet()) {
                if (entry.getKey() != null && entry.getValue() instanceof Number num) {
                    res.put(entry.getKey().toString(), num.doubleValue());
                }
            }
            return res;
        }
        return Collections.emptyMap();
    }
}
