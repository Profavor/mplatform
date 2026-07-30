package com.classification.domain_system.service;

import com.classification.domain_system.config.MdmProperties;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.service.dq.DqRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DqScheduledScanService {

    private final DomainRepository domainRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final RecordRepository recordRepository;
    private final DqRuleEngine dqRuleEngine;
    private final DqScoreSnapshotService dqScoreSnapshotService;
    private final MdmProperties mdmProperties;

    private final NotificationService notificationService;
    private final com.classification.domain_system.repository.UserRepository userRepository;

    @Scheduled(cron = "${dq.scan.cron:0 0 2 * * ?}")
    public void runScheduledDqScan() {
        log.info("[DQ Schedule] Starting automated periodic DQ scan...");
        List<Domain> activeDomains = domainRepository.findAll().stream()
                .filter(Domain::isAutoDqScanEnabled)
                .toList();

        int scannedDomainCount = 0;
        for (Domain domain : activeDomains) {
            try {
                Map<String, Object> scoreMap = dqRuleEngine.runDomainDqScan(domain.getId());
                long totalViolations = scoreMap.get("totalViolations") != null ? (long) scoreMap.get("totalViolations") : 0L;

                // 스캔 결과를 스냅샷으로 기록
                dqScoreSnapshotService.recordSnapshot(domain.getId(), scoreMap, "SCHEDULED");

                if (totalViolations > 0 && notificationService != null) {
                    String domainName = domain.getName() != null && domain.getName().containsKey("ko") 
                            ? domain.getName().get("ko") : domain.getId().toString();
                    String title = "DQ Scan Violation Detected";
                    String message = String.format("Domain [%s] has %d DQ violations.", domainName, totalViolations);

                    var users = userRepository.findAll();
                    if (!users.isEmpty()) {
                        for (var user : users) {
                            try {
                                notificationService.createNotification(
                                        user.getId(),
                                        title,
                                        message,
                                        "DQ_VIOLATION",
                                        "/domains/" + domain.getId() + "/dq"
                                );
                            } catch (Exception ex) {
                                log.warn("Failed to notify user {} for DQ violation: {}", user.getId(), ex.getMessage());
                            }
                        }
                    } else {
                        notificationService.createNotification(
                                mdmProperties.getDq().getSystemUserId(),
                                title,
                                message,
                                "DQ_VIOLATION",
                                "/domains/" + domain.getId() + "/dq"
                        );
                    }
                }
                scannedDomainCount++;
                log.info("[DQ Schedule] Completed auto scan for domain: {} ({}) with {} violations", domain.getName(), domain.getId(), totalViolations);
            } catch (Exception e) {
                log.error("[DQ Schedule] Failed auto scan for domain {}: {}", domain.getId(), e.getMessage(), e);
            }
        }
        log.info("[DQ Schedule] Completed automated periodic DQ scan. Total scanned domains: {}", scannedDomainCount);
    }
}

