package com.classification.domain_system.service;

import com.classification.domain_system.dto.SchemaImpactSimulationDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SchemaImpactSimulationServiceTest {

    @Mock private DomainRepository domainRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private IntegrationChannelRepository integrationChannelRepository;
    @Mock private DqRuleRepository dqRuleRepository;

    @InjectMocks
    private SchemaImpactAnalysisService schemaImpactAnalysisService;

    private UUID domainId;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        domain = new Domain();
        domain.setId(domainId);
    }

    @Test
    @DisplayName("simulateImpact: 필드 삭제 시 기존 데이터 및 연계 채널 영향도 감점 정상 산출")
    void testSimulateImpactDeleteWithDataAndChannels() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));

        Record r1 = new Record();
        r1.setData("{\"email\": \"user@test.com\"}");
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(r1));

        IntegrationChannel channel = new IntegrationChannel();
        channel.setName("CRM 연동");
        channel.setMappingConfigJson("{\"mapping\": {\"email\": \"user_email\"}}");
        when(integrationChannelRepository.findByIsActiveTrue()).thenReturn(List.of(channel));

        DqRule rule = new DqRule();
        FieldDefinition fd = new FieldDefinition();
        fd.setKey("email");
        rule.setFieldDefinition(fd);
        rule.setRuleType(DqRuleType.REGEX);
        when(dqRuleRepository.findByDomainIdAndIsActiveTrueOrderBySortOrderAsc(domainId)).thenReturn(List.of(rule));

        SchemaImpactSimulationDto.SimulationRequest request = SchemaImpactSimulationDto.SimulationRequest.builder()
                .fieldKey("email")
                .action("DELETE")
                .build();

        SchemaImpactSimulationDto.SimulationResponse response = schemaImpactAnalysisService.simulateImpact(domainId, request);

        assertThat(response).isNotNull();
        assertThat(response.getPopulatedRecordCount()).isEqualTo(1);
        assertThat(response.getAffectedChannels()).contains("CRM 연동");
        assertThat(response.getAffectedDqRules()).contains("REGEX");
        assertThat(response.getSafetyScore()).isLessThanOrEqualTo(30); // 100 - 40 - 30 - 20 = 10
        assertThat(response.getRiskLevel()).isEqualTo("CRITICAL");
        assertThat(response.getRecommendations()).hasSize(3);
    }

    @Test
    @DisplayName("simulateImpact: 빈 필드 삭제 시 100점 SAFE 산출")
    void testSimulateImpactSafe() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(Collections.emptyList());
        when(integrationChannelRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());
        when(dqRuleRepository.findByDomainIdAndIsActiveTrueOrderBySortOrderAsc(domainId)).thenReturn(Collections.emptyList());

        SchemaImpactSimulationDto.SimulationRequest request = SchemaImpactSimulationDto.SimulationRequest.builder()
                .fieldKey("unusedField")
                .action("DELETE")
                .build();

        SchemaImpactSimulationDto.SimulationResponse response = schemaImpactAnalysisService.simulateImpact(domainId, request);

        assertThat(response.getSafetyScore()).isEqualTo(100);
        assertThat(response.getRiskLevel()).isEqualTo("SAFE");
    }
}
