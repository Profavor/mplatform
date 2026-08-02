package com.classification.domain_system.service;

import com.classification.domain_system.dto.SchemaImpactAnalysisDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SchemaImpactAnalysisServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private IntegrationChannelRepository integrationChannelRepository;

    @Mock
    private com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;

    @InjectMocks
    private SchemaImpactAnalysisService schemaImpactAnalysisService;

    private Domain testDomain;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        testDomain = new Domain();
        testDomain.setId(domainId);
        testDomain.setName(Map.of("ko", "상품 도메인"));
    }

    @Test
    @DisplayName("필드 삭제 시 사전 영향도 분석 보고서를 정상 생성한다")
    void testAnalyzeFieldDeletionImpact() {
        // given
        given(domainRepository.findById(domainId)).willReturn(Optional.of(testDomain));
        given(recordRepository.countByNodeDomainIdAndStatus(domainId, "ACTIVE")).willReturn(150L);
        given(integrationChannelRepository.findByIsActiveTrue()).willReturn(Collections.emptyList());

        SchemaImpactAnalysisDto.ImpactAnalysisRequest request = new SchemaImpactAnalysisDto.ImpactAnalysisRequest();
        request.setChangeType("DELETE_FIELD");
        request.setFieldDefinitionId(UUID.randomUUID());

        // when
        SchemaImpactAnalysisDto.ImpactAnalysisResponse response = schemaImpactAnalysisService.analyzeImpact(domainId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getDomainId()).isEqualTo(domainId);
        assertThat(response.getTotalAffectedRecords()).isEqualTo(150L);
        assertThat(response.getRiskLevel()).isIn("MEDIUM", "HIGH", "CRITICAL");
        assertThat(response.getWarnings()).isNotEmpty();
    }
}
