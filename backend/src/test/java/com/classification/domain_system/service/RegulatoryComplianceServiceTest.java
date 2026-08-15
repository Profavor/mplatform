package com.classification.domain_system.service;

import com.classification.domain_system.dto.RegulatoryComplianceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RegulatoryComplianceServiceTest {

    private RegulatoryComplianceService complianceService;

    @BeforeEach
    void setUp() {
        complianceService = new RegulatoryComplianceService();
    }

    @Test
    @DisplayName("runAudit: ISMS-P 및 개인정보보호법 자체 감사 진단 실행")
    void testRunAudit() {
        RegulatoryComplianceDto.ComplianceAuditReport res = complianceService.runAudit();

        assertThat(res).isNotNull();
        assertThat(res.getOverallScore()).isEqualTo(100);
        assertThat(res.getCertificationReadiness()).isEqualTo("READY");
        assertThat(res.getItems()).hasSize(5);
        assertThat(res.getItems().get(0).getFramework()).isEqualTo("ISMS-P");
    }
}
