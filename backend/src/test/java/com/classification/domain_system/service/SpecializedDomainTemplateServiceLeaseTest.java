package com.classification.domain_system.service;

import com.classification.domain_system.base.BaseServiceTest;
import com.classification.domain_system.dto.DomainResponse;
import com.classification.domain_system.dto.SpecializedDomainProvisionRequest;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto;
import com.classification.domain_system.entity.*;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class SpecializedDomainTemplateServiceLeaseTest extends BaseServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private ClassificationAxisRepository axisRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;

    @Mock
    private DqRuleRepository dqRuleRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private FieldGroupRepository fieldGroupRepository;

    @Mock
    private CodeDetailRepository codeDetailRepository;

    @InjectMocks
    private SpecializedDomainTemplateService templateService;

    @Test
    @DisplayName("LEASE_CONTRACT 템플릿의 메타데이터, 분류축, 노드, 섹터, 필드, DQ 룰이 완전하게 정의되어 있다")
    void leaseContractTemplateIntegrity() {
        // when
        SpecializedDomainTemplateDto template = templateService.getTemplates().stream()
                .filter(t -> "LEASE_CONTRACT".equals(t.getCategory()))
                .findFirst()
                .orElse(null);

        // then
        assertThat(template).isNotNull();
        assertThat(template.getName().get("ko")).isEqualTo("부동산 임대차 마스터");
        assertThat(template.getName().get("en")).isEqualTo("Real Estate Lease Master");
        assertThat(template.getIcon()).isEqualTo("apartment");
        assertThat(template.getNumberingPattern()).isEqualTo("LEASE-{YYYY}-{SEQ:6}");
        assertThat(template.getAxisCode()).isEqualTo("LEASE_PROPERTY_TYPE");
        assertThat(template.getAxisName().get("ko")).isEqualTo("부동산 용도 분류");
        assertThat(template.getIdentifierFieldKey()).isEqualTo("contract_no");
        assertThat(template.getDisplayNameFieldKey()).isEqualTo("tenant_name");

        // 1. 분류 노드 검증 (대분류 2개: RESIDENTIAL, COMMERCIAL / 중분류 6개)
        List<SpecializedDomainTemplateDto.ClassificationNodeTemplateDto> nodes = template.getNodes();
        assertThat(nodes).hasSize(8);
        List<String> nodeCodes = nodes.stream().map(SpecializedDomainTemplateDto.ClassificationNodeTemplateDto::getCode).toList();
        assertThat(nodeCodes).containsExactlyInAnyOrder(
                "RESIDENTIAL", "APT", "MULTI_UNIT", "OFFICETEL_RES",
                "COMMERCIAL", "RETAIL", "OFFICE", "WAREHOUSE"
        );

        // 2. 4대 섹터 검증
        List<SpecializedDomainTemplateDto.SectorTemplateDto> sectors = template.getSectors();
        assertThat(sectors).hasSize(4);
        List<String> sectorCodes = sectors.stream().map(SpecializedDomainTemplateDto.SectorTemplateDto::getCode).toList();
        assertThat(sectorCodes).containsExactly(
                "PROPERTY_INFO", "CONTRACT_TERMS", "FINANCE_TERMS", "RISK_GOVERNANCE"
        );

        // 3. 필드 검증 (20개 필드)
        List<SpecializedDomainTemplateDto.FieldTemplateDto> fields = template.getFields();
        assertThat(fields).hasSize(20);
        List<String> fieldKeys = fields.stream().map(SpecializedDomainTemplateDto.FieldTemplateDto::getKey).toList();
        assertThat(fieldKeys).contains(
                "contract_no", "building_name", "address_primary", "unit_number", "exclusive_area",
                "tenant_name", "tenant_contact", "tenant_email", "lease_type",
                "contract_start_date", "contract_end_date", "deposit_amount", "monthly_rent",
                "maintenance_fee", "rent_payment_day", "prior_mortgage_amount",
                "market_price_estimate", "debt_ratio", "contract_status", "special_agreement"
        );

        // 4. DQ 룰 검증 (5종)
        List<SpecializedDomainTemplateDto.DqRuleTemplateDto> dqRules = template.getDqRules();
        assertThat(dqRules).hasSize(5);
        List<String> dqFieldKeys = dqRules.stream().map(SpecializedDomainTemplateDto.DqRuleTemplateDto::getFieldKey).toList();
        assertThat(dqFieldKeys).contains(
                "deposit_amount", "monthly_rent", "rent_payment_day", "tenant_contact", "contract_end_date"
        );
    }

    @Test
    @DisplayName("LEASE_CONTRACT 특화도메인 프로비저닝 시 도메인, 분류축, 노드, 섹터/그룹, 필드, DQ 룰이 정상 생성된다")
    void provisionLeaseContractDomainSuccessfully() {
        // given
        SpecializedDomainProvisionRequest request = SpecializedDomainProvisionRequest.builder()
                .category("LEASE_CONTRACT")
                .build();

        UUID domainId = UUID.randomUUID();
        UUID axisId = UUID.randomUUID();

        Domain savedDomain = new Domain();
        savedDomain.setId(domainId);
        savedDomain.setDomainType("SPECIALIZED");
        savedDomain.setSpecializedCategory("LEASE_CONTRACT");
        savedDomain.setName(Map.of("ko", "부동산 임대차 마스터", "en", "Real Estate Lease Master"));

        ClassificationAxis savedAxis = new ClassificationAxis();
        savedAxis.setId(axisId);
        savedAxis.setDomain(savedDomain);

        given(domainRepository.findBySpecializedCategory("LEASE_CONTRACT")).willReturn(Optional.empty());
        given(domainRepository.save(any(Domain.class))).willReturn(savedDomain);
        given(axisRepository.save(any(ClassificationAxis.class))).willReturn(savedAxis);
        given(nodeRepository.save(any(ClassificationNode.class))).willAnswer(inv -> {
            ClassificationNode n = inv.getArgument(0);
            if (n.getId() == null) n.setId(UUID.randomUUID());
            return n;
        });
        given(sectorRepository.save(any(Sector.class))).willAnswer(inv -> {
            Sector s = inv.getArgument(0);
            if (s.getId() == null) s.setId(UUID.randomUUID());
            return s;
        });
        given(fieldGroupRepository.save(any(FieldGroup.class))).willAnswer(inv -> {
            FieldGroup fg = inv.getArgument(0);
            if (fg.getId() == null) fg.setId(UUID.randomUUID());
            return fg;
        });
        given(fieldDefinitionRepository.save(any(FieldDefinition.class))).willAnswer(inv -> {
            FieldDefinition fd = inv.getArgument(0);
            if (fd.getId() == null) fd.setId(UUID.randomUUID());
            return fd;
        });
        given(dqRuleRepository.save(any(DqRule.class))).willAnswer(inv -> {
            DqRule r = inv.getArgument(0);
            if (r.getId() == null) r.setId(UUID.randomUUID());
            return r;
        });

        // when
        DomainResponse response = templateService.provisionDomain(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(domainId);

        // 노드 저장 검증 (8개 노드)
        ArgumentCaptor<ClassificationNode> nodeCaptor = ArgumentCaptor.forClass(ClassificationNode.class);
        verify(nodeRepository, atLeast(8)).save(nodeCaptor.capture());
        List<ClassificationNode> capturedNodes = nodeCaptor.getAllValues();
        List<ClassificationNode> rootLevelNodes = capturedNodes.stream()
                .filter(n -> n.getParent() == null)
                .toList();
        assertThat(rootLevelNodes).hasSize(2); // RESIDENTIAL, COMMERCIAL

        // 필드 정의 저장 검증 (20개 필드)
        ArgumentCaptor<FieldDefinition> fieldCaptor = ArgumentCaptor.forClass(FieldDefinition.class);
        verify(fieldDefinitionRepository, atLeast(20)).save(fieldCaptor.capture());

        // DQ 룰 저장 검증 (5개 룰)
        ArgumentCaptor<DqRule> dqCaptor = ArgumentCaptor.forClass(DqRule.class);
        verify(dqRuleRepository, times(5)).save(dqCaptor.capture());
    }
}
