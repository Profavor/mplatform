package com.classification.domain_system.service;

import com.classification.domain_system.base.BaseServiceTest;
import com.classification.domain_system.dto.DomainResponse;
import com.classification.domain_system.dto.SpecializedDomainProvisionRequest;
import com.classification.domain_system.dto.SpecializedDomainTemplateDto;
import com.classification.domain_system.entity.*;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class SpecializedDomainTemplateServiceTest extends BaseServiceTest {

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

    @InjectMocks
    private SpecializedDomainTemplateService templateService;

    @Nested
    @DisplayName("getTemplates")
    class GetTemplates {

        @Test
        @DisplayName("6개 핵심 특화도메인 템플릿 목록과 고도화된 섹터/그룹/필드를 정상 반환한다")
        void returnsAllSixTemplates() {
            List<SpecializedDomainTemplateDto> templates = templateService.getTemplates();

            assertThat(templates).hasSize(6);
            List<String> categories = templates.stream()
                    .map(SpecializedDomainTemplateDto::getCategory)
                    .toList();

            assertThat(categories).containsExactlyInAnyOrder(
                    "CUSTOMER", "VENDOR", "PRODUCT", "MATERIAL", "EMPLOYEE", "STOCK"
            );

            // 각 템플릿의 무결성 검증
            for (SpecializedDomainTemplateDto t : templates) {
                assertThat(t.getName()).isNotEmpty();
                assertThat(t.getIcon()).isNotBlank();
                assertThat(t.getNumberingPattern()).isNotBlank();
                assertThat(t.getAxisName()).isNotEmpty();
                assertThat(t.getAxisCode()).isNotBlank();
                assertThat(t.getRootNodeName()).isNotEmpty();
                assertThat(t.getNodes()).isNotEmpty();
                assertThat(t.getSectors()).isNotEmpty();
                assertThat(t.getFields()).hasSizeGreaterThanOrEqualTo(18);
                assertThat(t.getIdentifierFieldKey()).isNotBlank();
                assertThat(t.getDisplayNameFieldKey()).isNotBlank();
                assertThat(t.getDqRules()).isNotEmpty();

                // 식별자 및 표시명 필드가 fields 목록에 실재하는지 검증
                List<String> fieldKeys = t.getFields().stream()
                        .map(SpecializedDomainTemplateDto.FieldTemplateDto::getKey)
                        .toList();
                assertThat(fieldKeys).contains(t.getIdentifierFieldKey());
                assertThat(fieldKeys).contains(t.getDisplayNameFieldKey());

                // 모든 필드가 그룹코드를 가지고 있는지 검증
                for (SpecializedDomainTemplateDto.FieldTemplateDto ft : t.getFields()) {
                    assertThat(ft.getGroupCode()).isNotBlank();
                }
            }
        }

        @Test
        @DisplayName("모든 필드의 gridWidth는 12분할 체제(1~12) 범위여야 한다")
        void allFieldsGridWidthInTwelveColumnRange() {
            List<SpecializedDomainTemplateDto> templates = templateService.getTemplates();

            for (SpecializedDomainTemplateDto t : templates) {
                for (SpecializedDomainTemplateDto.FieldTemplateDto ft : t.getFields()) {
                    assertThat(ft.getGridWidth())
                            .as("도메인 [%s] 필드 [%s]의 gridWidth=%d는 1~12 범위여야 합니다.",
                                    t.getCategory(), ft.getKey(), ft.getGridWidth())
                            .isNotNull()
                            .isBetween(1, 12);
                }
            }
        }

        @Test
        @DisplayName("모든 필드는 적절한 AG-Grid 컬럼 너비(tableColumnWidth: 100~300px)를 가져야 한다")
        void allFieldsHaveValidTableColumnWidth() {
            List<SpecializedDomainTemplateDto> templates = templateService.getTemplates();

            for (SpecializedDomainTemplateDto t : templates) {
                for (SpecializedDomainTemplateDto.FieldTemplateDto ft : t.getFields()) {
                    assertThat(ft.getTableColumnWidth())
                            .as("도메인 [%s] 필드 [%s]의 tableColumnWidth는 100~300 범위여야 합니다.",
                                    t.getCategory(), ft.getKey())
                            .isNotNull()
                            .isBetween(100, 300);
                }
            }
        }

        @Test
        @DisplayName("SELECT 타입 필드의 모든 옵션에는 'key' 속성이 반드시 있어야 한다")
        void selectFieldOptionsHaveKeyProperty() throws Exception {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<SpecializedDomainTemplateDto> templates = templateService.getTemplates();

            for (SpecializedDomainTemplateDto t : templates) {
                for (SpecializedDomainTemplateDto.FieldTemplateDto ft : t.getFields()) {
                    if (!"SELECT".equals(ft.getType())) continue;
                    assertThat(ft.getOptions())
                            .as("도메인 [%s] SELECT 필드 [%s]의 options가 null이면 안 됩니다.", t.getCategory(), ft.getKey())
                            .isNotBlank();

                    List<Map<String, Object>> opts = mapper.readValue(
                            ft.getOptions(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
                    for (Map<String, Object> opt : opts) {
                        assertThat(opt)
                                .as("도메인 [%s] 필드 [%s]의 옵션 %s에 'key' 속성이 없습니다.", t.getCategory(), ft.getKey(), opt)
                                .containsKey("key");
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("provisionDomain")
    class ProvisionDomain {

        @Test
        @DisplayName("고객(CUSTOMER) 특화도메인을 섹터, 필드그룹, 하위노드와 함께 완벽하게 프로비저닝한다")
        void provisionsCustomerDomainSuccessfully() {
            // given
            SpecializedDomainProvisionRequest request = SpecializedDomainProvisionRequest.builder()
                    .category("CUSTOMER")
                    .build();

            UUID domainId = UUID.randomUUID();
            UUID axisId = UUID.randomUUID();
            UUID idFieldId = UUID.randomUUID();
            UUID nameFieldId = UUID.randomUUID();

            Domain savedDomain = new Domain();
            savedDomain.setId(domainId);
            savedDomain.setDomainType("SPECIALIZED");
            savedDomain.setSpecializedCategory("CUSTOMER");
            savedDomain.setName(Map.of("ko", "고객 마스터", "en", "Customer Master"));
            savedDomain.setIcon("person_pin");
            savedDomain.setNumberingPattern("CUST-{YYYY}-{SEQ:6}");

            ClassificationAxis savedAxis = new ClassificationAxis();
            savedAxis.setId(axisId);
            savedAxis.setDomain(savedDomain);

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

            // Field definition mock
            given(fieldDefinitionRepository.save(any(FieldDefinition.class))).willAnswer(invocation -> {
                FieldDefinition fd = invocation.getArgument(0);
                fd.setId("customer_no".equals(fd.getKey()) ? idFieldId : ("customer_name".equals(fd.getKey()) ? nameFieldId : UUID.randomUUID()));
                return fd;
            });

            // when
            DomainResponse response = templateService.provisionDomain(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDomainType()).isEqualTo("SPECIALIZED");
            assertThat(response.getSpecializedCategory()).isEqualTo("CUSTOMER");

            verify(domainRepository, atLeast(2)).save(any(Domain.class)); // 1차 생성 + 필드 바인딩 2차 저장
            verify(axisRepository).save(any(ClassificationAxis.class));
            verify(nodeRepository, atLeast(5)).save(any(ClassificationNode.class));
            verify(sectorRepository, atLeast(3)).save(any(Sector.class));
            verify(fieldGroupRepository, atLeast(5)).save(any(FieldGroup.class));
            org.mockito.ArgumentCaptor<FieldDefinition> fdCaptor = org.mockito.ArgumentCaptor.forClass(FieldDefinition.class);
            verify(fieldDefinitionRepository, atLeast(20)).save(fdCaptor.capture());
            assertThat(fdCaptor.getAllValues()).allMatch(f -> f.getTableColumnWidth() != null && f.getTableColumnWidth() >= 100);
            verify(dqRuleRepository, atLeast(2)).save(any(DqRule.class));
        }

        @Test
        @DisplayName("주식(STOCK) 특화도메인을 섹터, 필드그룹, 하위노드와 함께 완벽하게 프로비저닝한다")
        void provisionsStockDomainSuccessfully() {
            // given
            SpecializedDomainProvisionRequest request = SpecializedDomainProvisionRequest.builder()
                    .category("STOCK")
                    .build();

            UUID domainId = UUID.randomUUID();
            Domain savedDomain = new Domain();
            savedDomain.setId(domainId);
            savedDomain.setDomainType("SPECIALIZED");
            savedDomain.setSpecializedCategory("STOCK");
            savedDomain.setName(Map.of("ko", "주식 종목 마스터", "en", "Stock Master"));

            given(domainRepository.save(any(Domain.class))).willReturn(savedDomain);
            given(axisRepository.save(any(ClassificationAxis.class))).willReturn(new ClassificationAxis());
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
            given(fieldDefinitionRepository.save(any(FieldDefinition.class))).willAnswer(invocation -> {
                FieldDefinition fd = invocation.getArgument(0);
                fd.setId(UUID.randomUUID());
                return fd;
            });

            // when
            DomainResponse response = templateService.provisionDomain(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDomainType()).isEqualTo("SPECIALIZED");
            assertThat(response.getSpecializedCategory()).isEqualTo("STOCK");

            verify(domainRepository, atLeast(2)).save(any(Domain.class));
            verify(nodeRepository, atLeast(5)).save(any(ClassificationNode.class));
            verify(sectorRepository, atLeast(3)).save(any(Sector.class));
            verify(fieldGroupRepository, atLeast(4)).save(any(FieldGroup.class));
            verify(fieldDefinitionRepository, atLeast(18)).save(any(FieldDefinition.class));
        }
    }
}
