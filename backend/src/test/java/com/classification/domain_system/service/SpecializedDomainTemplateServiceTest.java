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

    @InjectMocks
    private SpecializedDomainTemplateService templateService;

    @Nested
    @DisplayName("getTemplates")
    class GetTemplates {

        @Test
        @DisplayName("6개 핵심 특화도메인 템플릿 목록을 정상 반환한다")
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
                assertThat(t.getFields()).isNotEmpty();
                assertThat(t.getIdentifierFieldKey()).isNotBlank();
                assertThat(t.getDisplayNameFieldKey()).isNotBlank();
                assertThat(t.getDqRules()).isNotEmpty();

                // 식별자 및 표시명 필드가 fields 목록에 실재하는지 검증
                List<String> fieldKeys = t.getFields().stream()
                        .map(SpecializedDomainTemplateDto.FieldTemplateDto::getKey)
                        .toList();
                assertThat(fieldKeys).contains(t.getIdentifierFieldKey());
                assertThat(fieldKeys).contains(t.getDisplayNameFieldKey());
            }
        }
    }

    @Nested
    @DisplayName("provisionDomain")
    class ProvisionDomain {

        @Test
        @DisplayName("고객(CUSTOMER) 특화도메인을 원클릭으로 완벽하게 프로비저닝한다")
        void provisionsCustomerDomainSuccessfully() {
            // given
            SpecializedDomainProvisionRequest request = SpecializedDomainProvisionRequest.builder()
                    .category("CUSTOMER")
                    .build();

            UUID domainId = UUID.randomUUID();
            UUID axisId = UUID.randomUUID();
            UUID nodeId = UUID.randomUUID();
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

            ClassificationNode savedNode = new ClassificationNode();
            savedNode.setId(nodeId);
            savedNode.setDomain(savedDomain);
            savedNode.setAxis(savedAxis);

            given(domainRepository.save(any(Domain.class))).willReturn(savedDomain);
            given(axisRepository.save(any(ClassificationAxis.class))).willReturn(savedAxis);
            given(nodeRepository.save(any(ClassificationNode.class))).willReturn(savedNode);

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
            verify(nodeRepository).save(any(ClassificationNode.class));
            verify(fieldDefinitionRepository, atLeast(7)).save(any(FieldDefinition.class));
            verify(dqRuleRepository, atLeast(2)).save(any(DqRule.class));
        }

        @Test
        @DisplayName("주식(STOCK) 특화도메인을 원클릭으로 완벽하게 프로비저닝한다")
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
            given(nodeRepository.save(any(ClassificationNode.class))).willReturn(new ClassificationNode());
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
            verify(fieldDefinitionRepository, atLeast(8)).save(any(FieldDefinition.class));
        }
    }
}
