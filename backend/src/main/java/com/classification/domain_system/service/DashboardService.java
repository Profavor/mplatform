package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
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
import com.classification.domain_system.dto.LeaseSummaryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;
import java.util.Optional;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DomainRepository domainRepository;
    private final RecordRepository recordRepository;
    private final ApprovalRequestRepository approvalRepository;
    private final MatchCandidateRepository matchCandidateRepository;
    private final DqViolationRepository dqViolationRepository;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @org.springframework.cache.annotation.Cacheable(value = "dashboardStats")
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
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

    @Transactional(readOnly = true)
    public LeaseSummaryDto getLeaseSummary(UUID organizationId) {
        Optional<Domain> leaseDomainOpt = organizationId != null
                ? domainRepository.findBySpecializedCategoryAndOrganizationId("LEASE_CONTRACT", organizationId)
                : domainRepository.findBySpecializedCategory("LEASE_CONTRACT");

        if (leaseDomainOpt.isEmpty()) {
            return LeaseSummaryDto.builder()
                    .hasLeaseDomain(false)
                    .totalContracts(0)
                    .expiringWithin30Days(0)
                    .expiredContracts(0)
                    .overdueCount(0)
                    .highDebtRatioCount(0)
                    .totalDepositAmount(0)
                    .totalMonthlyRent(0)
                    .urgentAlerts(Collections.emptyList())
                    .build();
        }

        Domain domain = leaseDomainOpt.get();
        List<Record> records = recordRepository.findAllByDomainId(domain.getId());
        if (records == null) records = Collections.emptyList();

        long expiringWithin30Days = 0;
        long expiredContracts = 0;
        long overdueCount = 0;
        long highDebtRatioCount = 0;
        long totalDepositAmount = 0;
        long totalMonthlyRent = 0;

        LocalDate today = LocalDate.now();
        List<LeaseSummaryDto.UrgentAlertDto> alerts = new ArrayList<>();

        ObjectMapper mapper = this.objectMapper != null ? this.objectMapper : new ObjectMapper();

        for (Record r : records) {
            if (r == null || r.getData() == null || r.getData().isBlank()) continue;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = mapper.readValue(r.getData(), Map.class);
                if (data == null) continue;

                String contractNo = getString(data, "contract_no");
                String buildingName = getString(data, "building_name");
                String unitNumber = getString(data, "unit_number");
                String tenantName = getString(data, "tenant_name");
                String tenantContact = getString(data, "tenant_contact");
                String endDateStr = getString(data, "contract_end_date");
                String contractStatus = getString(data, "contract_status");

                long deposit = getLong(data, "deposit_amount");
                long rent = getLong(data, "monthly_rent");
                double debtRatio = getDouble(data, "debt_ratio");

                totalDepositAmount += deposit;
                totalMonthlyRent += rent;

                boolean isOverdue = "OVERDUE".equalsIgnoreCase(contractStatus);
                if (isOverdue) {
                    overdueCount++;
                }

                boolean isHighDebt = debtRatio >= 80.0;
                if (isHighDebt) {
                    highDebtRatioCount++;
                }

                LocalDate endDate = null;
                Long daysRemaining = null;
                boolean isExpiringSoon = false;
                boolean isExpired = false;

                if (endDateStr != null && !endDateStr.isBlank()) {
                    try {
                        endDate = LocalDate.parse(endDateStr.trim().substring(0, 10));
                        daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, endDate);
                        if (daysRemaining < 0) {
                            expiredContracts++;
                            isExpired = true;
                        } else if (daysRemaining <= 30) {
                            expiringWithin30Days++;
                            isExpiringSoon = true;
                        }
                    } catch (Exception ignored) {
                    }
                }

                // 긴급 조치 알림 대상 선정 (만기 30일 이내, 연체, 고부채비율, 이미 만기됨)
                if (isExpiringSoon || isOverdue || isHighDebt || isExpired) {
                    String riskType = isOverdue ? "OVERDUE"
                            : (isExpiringSoon ? "EXPIRING_SOON"
                            : (isHighDebt ? "HIGH_DEBT" : "EXPIRED"));

                    alerts.add(LeaseSummaryDto.UrgentAlertDto.builder()
                            .recordId(r.getId())
                            .contractNo(contractNo != null && !contractNo.isBlank() ? contractNo : "LEASE-" + r.getId().toString().substring(0, 8))
                            .buildingName(buildingName)
                            .unitNumber(unitNumber)
                            .tenantName(tenantName)
                            .tenantContact(tenantContact)
                            .endDate(endDateStr)
                            .daysRemaining(daysRemaining)
                            .riskType(riskType)
                            .debtRatio(debtRatio)
                            .monthlyRent(rent)
                            .depositAmount(deposit)
                            .build());
                }
            } catch (Exception ignored) {
            }
        }

        // 정렬: OVERDUE 우선, 그 다음 daysRemaining 오름차순 (만기 가장 임박한 순)
        alerts.sort((a, b) -> {
            if ("OVERDUE".equals(a.getRiskType()) && !"OVERDUE".equals(b.getRiskType())) return -1;
            if (!"OVERDUE".equals(a.getRiskType()) && "OVERDUE".equals(b.getRiskType())) return 1;
            if (a.getDaysRemaining() != null && b.getDaysRemaining() != null) {
                return a.getDaysRemaining().compareTo(b.getDaysRemaining());
            }
            return 0;
        });

        List<LeaseSummaryDto.UrgentAlertDto> limitedAlerts = alerts.stream().limit(10).toList();
        String domNameKo = domain.getName() != null && domain.getName().get("ko") != null
                ? domain.getName().get("ko") : "부동산 임대차 마스터";

        return LeaseSummaryDto.builder()
                .hasLeaseDomain(true)
                .domainId(domain.getId())
                .domainName(domNameKo)
                .totalContracts(records.size())
                .expiringWithin30Days(expiringWithin30Days)
                .expiredContracts(expiredContracts)
                .overdueCount(overdueCount)
                .highDebtRatioCount(highDebtRatioCount)
                .totalDepositAmount(totalDepositAmount)
                .totalMonthlyRent(totalMonthlyRent)
                .urgentAlerts(limitedAlerts)
                .build();
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase());
        return val != null ? String.valueOf(val) : null;
    }

    private long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase());
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String str) {
            try { return Long.parseLong(str.replaceAll("[^0-9-]", "")); } catch (Exception ignored) {}
        }
        return 0L;
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase());
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String str) {
            try { return Double.parseDouble(str.replaceAll("[^0-9.-]", "")); } catch (Exception ignored) {}
        }
        return 0.0;
    }
}
