package com.classification.domain_system.service;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.dq.DqRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DqScheduledScanServiceTest {

    @Mock
    private DomainRepository domainRepository;
    @Mock
    private DqRuleEngine dqRuleEngine;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DqScheduledScanService dqScheduledScanService;

    private Domain testDomain;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        testDomain = new Domain();
        testDomain.setId(domainId);
        testDomain.setAutoDqScanEnabled(true);
        testDomain.setName(Map.of("ko", "고객 도메인"));
    }

    @Test
    @DisplayName("runScheduledDqScan - 자동 스캔 활성화된 도메인 스캔 및 위반 발생 시 알림 생성")
    void runScheduledDqScan_Success_WithNotifications() {
        User user = new User();
        user.setId(UUID.randomUUID().toString());

        when(domainRepository.findAll()).thenReturn(List.of(testDomain));
        when(dqRuleEngine.runDomainDqScan(domainId)).thenReturn(Map.of("totalViolations", 5L));
        when(userRepository.findAll()).thenReturn(List.of(user));

        dqScheduledScanService.runScheduledDqScan();

        verify(dqRuleEngine, times(1)).runDomainDqScan(domainId);
        verify(notificationService, times(1)).createNotification(
                eq(UUID.fromString(user.getId())),
                contains("Violation"),
                contains("고객 도메인"),
                eq("DQ_VIOLATION"),
                anyString()
        );
    }
}
