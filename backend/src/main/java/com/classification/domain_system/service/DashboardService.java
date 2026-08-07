package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.MatchCandidateRepository;
import com.classification.domain_system.repository.DqViolationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DomainRepository domainRepository;
    private final RecordRepository recordRepository;
    private final ApprovalRequestRepository approvalRepository;
    private final MatchCandidateRepository matchCandidateRepository;
    private final DqViolationRepository dqViolationRepository;

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalDomains", domainRepository.count());
        stats.put("pendingApprovals", approvalRepository.findByStatusOrderByCreatedAtDesc("PENDING", Pageable.unpaged()).getTotalElements());
        stats.put("approvedApprovals", approvalRepository.findByStatusOrderByCreatedAtDesc("APPROVED", Pageable.unpaged()).getTotalElements());
        stats.put("rejectedApprovals", approvalRepository.findByStatusOrderByCreatedAtDesc("REJECTED", Pageable.unpaged()).getTotalElements());
        stats.put("activeRecords", recordRepository.countByStatus("ACTIVE"));
        stats.put("pendingMatches", matchCandidateRepository.countByStatus("PENDING"));
        stats.put("openDqViolations", dqViolationRepository.countByResolvedFalse());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getApprovalTrends() {
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        List<ApprovalRequest> requests = approvalRepository.findByCreatedAtAfter(sevenDaysAgo);
        
        Map<LocalDate, Long> countsByDate = requests.stream()
                .filter(r -> r.getCreatedAt() != null)
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate(), Collectors.counting()));

        List<Map<String, Object>> trends = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", countsByDate.getOrDefault(date, 0L));
            trends.add(item);
        }
        return trends;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDqTrends() {
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        List<com.classification.domain_system.entity.DqViolation> violations = dqViolationRepository.findAll().stream()
                .filter(v -> v.getCheckedAt().isAfter(sevenDaysAgo))
                .toList();

        Map<LocalDate, Long> countsByDate = violations.stream()
                .filter(v -> v.getCheckedAt() != null)
                .collect(Collectors.groupingBy(v -> v.getCheckedAt().toLocalDate(), Collectors.counting()));

        List<Map<String, Object>> trends = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", countsByDate.getOrDefault(date, 0L));
            trends.add(item);
        }
        return trends;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDqSeverityDistribution() {
        List<com.classification.domain_system.entity.DqViolation> violations = dqViolationRepository.findAll().stream()
                .filter(v -> !v.getResolved())
                .toList();
        Map<String, Long> counts = violations.stream()
                .collect(Collectors.groupingBy(com.classification.domain_system.entity.DqViolation::getSeverity, Collectors.counting()));
        
        return counts.entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("severity", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDomainDistribution() {
        List<Domain> domains = domainRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Domain domain : domains) {
            long recordCount = recordRepository.countByNodeDomainIdAndStatus(domain.getId(), "ACTIVE");
            Map<String, Object> map = new HashMap<>();
            map.put("domainId", domain.getId());
            map.put("domainName", domain.getName());
            map.put("recordCount", recordCount);
            result.add(map);
        }
        return result;
    }
}
