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

    @Mock
    private CodeDetailRepository codeDetailRepository;

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
        @DisplayName("모든 템플릿의 필드 타입은 공통코드(FIELD_TYPE)에 정의된 유효 타입이어야 한다")
        void allFieldsUseValidCommonCodeFieldTypes() {
            Set<String> validCommonCodeFieldTypes = Set.of(
                    "TEXT", "NUMBER", "DATE", "BOOLEAN", "JSON", "SELECT",
                    "DOMAIN_REFERENCE", "TIME", "HTML_TEXT", "CALCULATED",
                    "MULTILINGUAL", "FILE", "IMAGE", "DATE_RANGE", "EMAIL"
            );

            List<SpecializedDomainTemplateDto> templates = templateService.getTemplates();

            for (SpecializedDomainTemplateDto t : templates) {
                for (SpecializedDomainTemplateDto.FieldTemplateDto ft : t.getFields()) {
                    assertThat(validCommonCodeFieldTypes)
                            .as("도메인 [%s] 필드 [%s]의 타입 '%s'은 공통코드(FIELD_TYPE)에 정의되어 있어야 합니다.",
                                    t.getCategory(), ft.getKey(), ft.getType())
                            .contains(ft.getType());
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
        @DisplayName("고객(CUSTOMER) 특화도메인 프로비저닝 시 최상위 '전체' 더미 노드 없이 1차 분류노드가 최상위로 생성된다")
        void provisionsCustomerDomainWithoutDummyRootNode() {
            // given
            SpecializedDomainProvisionRequest request = SpecializedDomainProvisionRequest.builder()
                    .category("CUSTOMER")
                    .build();

            UUID domainId = UUID.randomUUID();
            UUID axisId = UUID.randomUUID();

            Domain savedDomain = new Domain();
            savedDomain.setId(domainId);
            savedDomain.setDomainType("SPECIALIZED");
            savedDomain.setSpecializedCategory("CUSTOMER");
            savedDomain.setName(Map.of("ko", "고객 마스터", "en", "Customer Master"));

            ClassificationAxis savedAxis = new ClassificationAxis();
            savedAxis.setId(axisId);
            savedAxis.setDomain(savedDomain);

            given(domainRepository.findBySpecializedCategory("CUSTOMER")).willReturn(Optional.empty());
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

            // when
            DomainResponse response = templateService.provisionDomain(request);

            // then
            assertThat(response).isNotNull();
            org.mockito.ArgumentCaptor<ClassificationNode> nodeCaptor = org.mockito.ArgumentCaptor.forClass(ClassificationNode.class);
            verify(nodeRepository, atLeast(8)).save(nodeCaptor.capture());

            // '전체 고객' 같은 더미 노드가 생성되지 않았는지 확인
            List<ClassificationNode> capturedNodes = nodeCaptor.getAllValues();
            assertThat(capturedNodes).noneMatch(n -> {
                Map<String, String> nameMap = n.getName();
                return nameMap != null && ("전체 고객".equals(nameMap.get("ko")) || "All Customers".equals(nameMap.get("en")));
            });

            // 최상위 1차 노드(INDIVIDUAL, CORPORATE, PROSPECT)들의 parent는 null이어야 함
            List<ClassificationNode> rootLevelNodes = capturedNodes.stream()
                    .filter(n -> n.getParent() == null)
                    .toList();
            assertThat(rootLevelNodes).hasSize(3); // INDIVIDUAL, CORPORATE, PROSPECT

            // 생성된 모든 필드는 노드가 아닌 도메인 레벨에 할당(definedAtNode == null, domain != null)되어야 함
            org.mockito.ArgumentCaptor<FieldDefinition> fieldCaptor = org.mockito.ArgumentCaptor.forClass(FieldDefinition.class);
            verify(fieldDefinitionRepository, atLeast(10)).save(fieldCaptor.capture());
            List<FieldDefinition> capturedFields = fieldCaptor.getAllValues();
            assertThat(capturedFields).allSatisfy(field -> {
                assertThat(field.getDomain()).isNotNull();
                assertThat(field.getDomain().getId()).isEqualTo(domainId);
                assertThat(field.getDefinedAtNode()).isNull();
            });
        }

        @Test
        @DisplayName("이미 존재하는 특화도메인에 대해 다시 만들기를 수행하면 중복 생성 없이 기존 도메인에 머지(Merge)된다")
        void mergeExistingDomainWhenProvisioningAgain() {
            // given
            SpecializedDomainProvisionRequest request = SpecializedDomainProvisionRequest.builder()
                    .category("CUSTOMER")
                    .name(Map.of("ko", "업데이트된 고객 마스터", "en", "Updated Customer Master"))
                    .build();

            UUID existingDomainId = UUID.randomUUID();
            Domain existingDomain = new Domain();
            existingDomain.setId(existingDomainId);
            existingDomain.setDomainType("SPECIALIZED");
            existingDomain.setSpecializedCategory("CUSTOMER");
            existingDomain.setName(Map.of("ko", "기존 고객 마스터", "en", "Old Customer Master"));

            ClassificationAxis existingAxis = new ClassificationAxis();
            existingAxis.setId(UUID.randomUUID());
            existingAxis.setAxisCode("CUSTOMER_TYPE");
            existingAxis.setDomain(existingDomain);

            FieldDefinition existingFd = new FieldDefinition();
            existingFd.setId(UUID.randomUUID());
            existingFd.setDomain(existingDomain);
            existingFd.setKey("customer_no");
            existingFd.setType("TEXT");

            given(domainRepository.findBySpecializedCategory("CUSTOMER")).willReturn(Optional.of(existingDomain));
            given(domainRepository.save(any(Domain.class))).willAnswer(inv -> inv.getArgument(0));
            given(axisRepository.findByDomainIdOrderBySortOrderAsc(existingDomainId)).willReturn(List.of(existingAxis));
            given(nodeRepository.findByDomain_Id(existingDomainId)).willReturn(Collections.emptyList());
            given(sectorRepository.findByDomainIdOrderBySortOrderAsc(existingDomainId)).willReturn(Collections.emptyList());
            given(fieldGroupRepository.findByDomainIdOrderBySortOrderAsc(existingDomainId)).willReturn(Collections.emptyList());
            given(fieldDefinitionRepository.findByDomain_Id(existingDomainId)).willReturn(List.of(existingFd));
            given(dqRuleRepository.findByDomainId(existingDomainId)).willReturn(Collections.emptyList());

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

            // when
            DomainResponse response = templateService.provisionDomain(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(existingDomainId);
            assertThat(existingDomain.getName().get("ko")).isEqualTo("업데이트된 고객 마스터");

            // 새로운 Axis가 생성되는 대신 기존 Axis가 재사용되었는지 확인
            verify(axisRepository, never()).save(any(ClassificationAxis.class));
            // 기존 customer_no 필드가 업데이트되고 추가 필드들이 save 되었는지 확인
            verify(fieldDefinitionRepository, atLeast(20)).save(any(FieldDefinition.class));
        }
    }
}
