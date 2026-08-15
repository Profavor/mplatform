package com.classification.domain_system.service;

import com.classification.domain_system.dto.RegulatoryComplianceDto;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.MenuAccessLogRepository;
import com.classification.domain_system.repository.SensitiveDataAccessLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class RegulatoryComplianceServiceTest {

    @Mock
    private SensitiveDataAccessLogRepository sensitiveLogRepository;

    @Mock
    private MenuAccessLogRepository menuAccessLogRepository;

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private RegulatoryComplianceService complianceService;

    @BeforeEach
    void setUp() {
        given(sensitiveLogRepository.count()).willReturn(200L);
        given(menuAccessLogRepository.count()).willReturn(300L);
        given(domainRepository.count()).willReturn(5L);
    }

    @Test
    @DisplayName("runAudit: ISMS-P/PIPA/GDPR 3대 규제 컴플라이언스 자동 진단 리포트 (DB 동적 연동)")
    void testRunAudit() {
        RegulatoryComplianceDto.ComplianceAuditReport res = complianceService.runAudit();

        assertThat(res).isNotNull();
        assertThat(res.getItems()).hasSize(5);
        assertThat(res.getPassedCount()).isEqualTo(5);
        assertThat(res.getOverallScore()).isEqualTo(100);
        assertThat(res.getCertificationReadiness()).isEqualTo("READY");
    }
}
