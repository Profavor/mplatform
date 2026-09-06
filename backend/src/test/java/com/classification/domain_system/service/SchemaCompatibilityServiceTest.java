package com.classification.domain_system.service;

import com.classification.domain_system.dto.SchemaCompatibilityDto;
import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.repository.DqRuleRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SchemaCompatibilityServiceTest {

    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private DqRuleRepository dqRuleRepository;
    @Mock private IntegrationChannelRepository integrationChannelRepository;

    @InjectMocks
    private SchemaCompatibilityService compatibilityService;

    private UUID domainId;
    private UUID fieldId;
    private FieldDefinition testField;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        fieldId = UUID.randomUUID();

        testField = new FieldDefinition();
        testField.setId(fieldId);
        testField.setKey("biz_reg_no");
        testField.setName(Map.of("ko", "사업자등록번호", "en", "Business Reg No"));
        testField.setType("STRING");
        testField.setRequired(false);
    }

    @Test
    @DisplayName("analyzeCompatibility: 필수값 전환 및 필드 삭제 시 브레이킹 체인지 감지")
    void testAnalyzeCompatibilityBreaking() {
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(Collections.emptyList());

        SchemaCompatibilityDto.SchemaCompatibilityReport res = compatibilityService.analyzeCompatibility(domainId, "필드 삭제 및 REQUIRED 전환");

        assertThat(res).isNotNull();
        assertThat(res.getOverallCompatibility()).isEqualTo("BREAKING_CHANGE");
        assertThat(res.getRiskScore()).isGreaterThan(50);
        assertThat(res.getRisks()).isNotEmpty();
    }

    @Test
    @DisplayName("analyzeCompatibility: 하위 호환 가능한 스키마 변경")
    void testAnalyzeCompatibilityCompatible() {
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(Collections.emptyList());

        SchemaCompatibilityDto.SchemaCompatibilityReport res = compatibilityService.analyzeCompatibility(domainId, "신규 OPTIONAL 필드 추가");

        assertThat(res).isNotNull();
        assertThat(res.getOverallCompatibility()).isEqualTo("COMPATIBLE");
        assertThat(res.getRiskScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("analyzeFieldImpact: 실제 데이터가 존재하는 필드 삭제 시 BREAKING 체인지 감지")
    void analyzeFieldImpact_DropFieldWithExistingRecords_DetectsCriticalBreakingChange() {
        when(fieldDefinitionRepository.findById(fieldId)).thenReturn(Optional.of(testField));
        // 전체 1,000건 중 850건 데이터 존재
        when(jdbcTemplate.queryForObject(contains("COUNT(*) FROM record r"), eq(Integer.class), eq(domainId)))
                .thenReturn(1000);
        when(jdbcTemplate.queryForObject(contains("r.data->>? IS NOT NULL"), eq(Integer.class), eq(domainId), eq("biz_reg_no"), eq("biz_reg_no")))
                .thenReturn(850);

        SchemaCompatibilityDto.FieldImpactSimulationRequest req = SchemaCompatibilityDto.FieldImpactSimulationRequest.builder()
                .fieldDefinitionId(fieldId)
                .changeType("DROP")
                .build();

        SchemaCompatibilityDto.FieldImpactReport report = compatibilityService.analyzeFieldImpact(domainId, req);

        assertThat(report).isNotNull();
        assertThat(report.getCompatibilityStatus()).isEqualTo("BREAKING");
        assertThat(report.getRiskScore()).isGreaterThanOrEqualTo(90);
        assertThat(report.getTotalDomainRecords()).isEqualTo(1000L);
        assertThat(report.getAffectedRecordCount()).isEqualTo(850L);
        assertThat(report.getAffectedRecordPercentage()).isEqualTo(85.0);
        assertThat(report.getRisks()).isNotEmpty();
        assertThat(report.getMitigationGuides()).isNotEmpty();
    }

    @Test
    @DisplayName("analyzeFieldImpact: 데이터가 없는 미사용 필드 삭제 시 SAFE 호환 판정")
    void analyzeFieldImpact_UnusedFieldDrop_ReturnsSafeCompatible() {
        when(fieldDefinitionRepository.findById(fieldId)).thenReturn(Optional.of(testField));
        when(jdbcTemplate.queryForObject(contains("COUNT(*) FROM record r"), eq(Integer.class), eq(domainId)))
                .thenReturn(1000);
        when(jdbcTemplate.queryForObject(contains("r.data->>? IS NOT NULL"), eq(Integer.class), eq(domainId), eq("biz_reg_no"), eq("biz_reg_no")))
                .thenReturn(0);

        SchemaCompatibilityDto.FieldImpactSimulationRequest req = SchemaCompatibilityDto.FieldImpactSimulationRequest.builder()
                .fieldDefinitionId(fieldId)
                .changeType("DROP")
                .build();

        SchemaCompatibilityDto.FieldImpactReport report = compatibilityService.analyzeFieldImpact(domainId, req);

        assertThat(report).isNotNull();
        assertThat(report.getCompatibilityStatus()).isEqualTo("SAFE");
        assertThat(report.getAffectedRecordCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("analyzeFieldImpact: 빈 값이 존재하는 필드를 필수값(MAKE_REQUIRED)으로 전환 시 BREAKING 감지")
    void analyzeFieldImpact_MakeRequiredWithMissingValues_DetectsCriticalBreakingChange() {
        when(fieldDefinitionRepository.findById(fieldId)).thenReturn(Optional.of(testField));
        // 전체 1000건 중 400건만 채워져 있어 600건 누락
        when(jdbcTemplate.queryForObject(contains("COUNT(*) FROM record r"), eq(Integer.class), eq(domainId)))
                .thenReturn(1000);
        when(jdbcTemplate.queryForObject(contains("r.data->>? IS NOT NULL"), eq(Integer.class), eq(domainId), eq("biz_reg_no"), eq("biz_reg_no")))
                .thenReturn(400);

        SchemaCompatibilityDto.FieldImpactSimulationRequest req = SchemaCompatibilityDto.FieldImpactSimulationRequest.builder()
                .fieldDefinitionId(fieldId)
                .changeType("MAKE_REQUIRED")
                .newRequired(true)
                .build();

        SchemaCompatibilityDto.FieldImpactReport report = compatibilityService.analyzeFieldImpact(domainId, req);

        assertThat(report).isNotNull();
        assertThat(report.getCompatibilityStatus()).isEqualTo("BREAKING");
        assertThat(report.getRiskScore()).isGreaterThanOrEqualTo(80);
        assertThat(report.getSummary()).contains("브레이킹 체인지");
    }

    @Test
    @DisplayName("analyzeFieldImpact: 필드 타입 변경(TYPE_CHANGE) 시 WARNING 및 사전 정제 가이드 제시")
    void analyzeFieldImpact_TypeChange_DetectsWarningRisk() {
        when(fieldDefinitionRepository.findById(fieldId)).thenReturn(Optional.of(testField));
        when(jdbcTemplate.queryForObject(contains("COUNT(*) FROM record r"), eq(Integer.class), eq(domainId)))
                .thenReturn(500);
        when(jdbcTemplate.queryForObject(contains("r.data->>? IS NOT NULL"), eq(Integer.class), eq(domainId), eq("biz_reg_no"), eq("biz_reg_no")))
                .thenReturn(500);

        SchemaCompatibilityDto.FieldImpactSimulationRequest req = SchemaCompatibilityDto.FieldImpactSimulationRequest.builder()
                .fieldDefinitionId(fieldId)
                .changeType("TYPE_CHANGE")
                .newType("NUMBER")
                .build();

        SchemaCompatibilityDto.FieldImpactReport report = compatibilityService.analyzeFieldImpact(domainId, req);

        assertThat(report).isNotNull();
        assertThat(report.getCompatibilityStatus()).isEqualTo("WARNING");
        assertThat(report.getRisks()).anyMatch(r -> "TYPE_CHANGED".equals(r.getChangeType()));
    }

    @Test
    @DisplayName("analyzeFieldImpact: 연관된 DQ 규칙 및 연계 채널을 정확히 탐지")
    void analyzeFieldImpact_DetectsAffectedDqRulesAndChannels() {
        when(fieldDefinitionRepository.findById(fieldId)).thenReturn(Optional.of(testField));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any()))
                .thenReturn(100);

        // 연관 DQ Rule 모킹
        DqRule rule = new DqRule();
        rule.setRuleType(DqRuleType.REGEX);
        rule.setMessage(Map.of("ko", "사업자등록번호 형식 검증", "en", "Format validation"));
        when(dqRuleRepository.findByFieldDefinition_IdOrderBySortOrderAsc(fieldId))
                .thenReturn(List.of(rule));

        // 연관 IntegrationChannel 모킹
        IntegrationChannel channel = new IntegrationChannel();
        channel.setName("ERP 고객 연동 채널");
        channel.setType("WEB_SERVICE");
        channel.setMappingConfigJson("{\"mappings\": [{\"source\": \"biz_reg_no\"}]}");
        when(integrationChannelRepository.findAll()).thenReturn(List.of(channel));

        SchemaCompatibilityDto.FieldImpactSimulationRequest req = SchemaCompatibilityDto.FieldImpactSimulationRequest.builder()
                .fieldDefinitionId(fieldId)
                .changeType("DROP")
                .build();

        SchemaCompatibilityDto.FieldImpactReport report = compatibilityService.analyzeFieldImpact(domainId, req);

        assertThat(report).isNotNull();
        assertThat(report.getAffectedDqRules()).hasSize(1);
        assertThat(report.getAffectedDqRules().get(0)).contains("REGEX");
        assertThat(report.getAffectedChannels()).hasSize(1);
        assertThat(report.getAffectedChannels().get(0)).contains("ERP 고객 연동 채널");
    }
}
