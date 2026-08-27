package com.classification.domain_system.controller;

import com.classification.domain_system.service.DomainService;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.service.FieldGroupService;
import com.classification.domain_system.service.SectorService;
import com.classification.domain_system.service.DomainPackageService;
import com.classification.domain_system.service.DqScoreSnapshotService;
import com.classification.domain_system.service.dq.DqRuleEngine;
import com.classification.domain_system.repository.DqRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DomainControllerDqTest {

    private DomainController controller;
    private DomainService domainService;
    private FieldDefinitionService fieldService;
    private SectorService sectorService;
    private FieldGroupService fieldGroupService;
    private DqRuleEngine dqRuleEngine;
    private DqRuleRepository dqRuleRepository;
    private DqScoreSnapshotService dqScoreSnapshotService;

    @BeforeEach
    void setUp() {
        domainService = mock(DomainService.class);
        fieldService = mock(FieldDefinitionService.class);
        sectorService = mock(SectorService.class);
        fieldGroupService = mock(FieldGroupService.class);
        DomainPackageService domainPackageService = mock(DomainPackageService.class);
        com.classification.domain_system.service.SpecializedDomainTemplateService specializedDomainTemplateService = mock(com.classification.domain_system.service.SpecializedDomainTemplateService.class);
        dqRuleEngine = mock(DqRuleEngine.class);
        dqRuleRepository = mock(DqRuleRepository.class);
        dqScoreSnapshotService = mock(DqScoreSnapshotService.class);

        controller = new DomainController(
                domainService, fieldService, sectorService, fieldGroupService,
                domainPackageService, specializedDomainTemplateService, dqRuleEngine, dqRuleRepository, dqScoreSnapshotService
        );
    }

    @Test
    @DisplayName("GET dq-score 조회가 runDomainDqScan을 실행하지 않고 순수 getDomainDqScore만 호출")
    void getDqScore_ShouldCallGetDomainDqScoreOnly() {
        UUID domainId = UUID.randomUUID();
        when(dqRuleEngine.getDomainDqScore(domainId)).thenReturn(Map.of("score", 100.0));

        ResponseEntity<Map<String, Object>> response = controller.getDomainDqScore(domainId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("score", 100.0);

        verify(dqRuleEngine, times(1)).getDomainDqScore(domainId);
        verify(dqRuleEngine, never()).runDomainDqScan(any());
        verifyNoInteractions(dqRuleRepository);
    }

    @Test
    @DisplayName("POST dq-scan 재스캔 요청이 runDomainDqScan을 실행")
    void runDqScan_ShouldTriggerScan() {
        UUID domainId = UUID.randomUUID();
        when(dqRuleEngine.runDomainDqScan(domainId)).thenReturn(Map.of("score", 95.0));

        ResponseEntity<Map<String, Object>> response = controller.runDomainDqScan(domainId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("score", 95.0);

        verify(dqRuleEngine, times(1)).runDomainDqScan(domainId);
    }

    @Test
    @DisplayName("GET dq-violations 요청이 dqRuleEngine.getDomainDqViolations를 정상 호출")
    void getDqViolations_ShouldCallGetDomainDqViolations() {
        UUID domainId = UUID.randomUUID();
        com.classification.domain_system.dto.PageResponse<com.classification.domain_system.dto.DqViolationResponse> pageResponse =
                com.classification.domain_system.dto.PageResponse.of(new org.springframework.data.domain.PageImpl<>(java.util.List.of()));
        when(dqRuleEngine.getDomainDqViolations(eq(domainId), any(), any(), any())).thenReturn(pageResponse);

        ResponseEntity<com.classification.domain_system.dto.PageResponse<com.classification.domain_system.dto.DqViolationResponse>> response =
                controller.getDomainDqViolations(domainId, "ERROR", "department", 0, 10);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();

        verify(dqRuleEngine, times(1)).getDomainDqViolations(eq(domainId), eq("ERROR"), eq("department"), any());
    }
}
