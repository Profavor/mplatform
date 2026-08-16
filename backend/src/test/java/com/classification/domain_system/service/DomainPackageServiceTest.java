package com.classification.domain_system.service;

import com.classification.domain_system.dto.DomainPackageDto;
import com.classification.domain_system.dto.DomainPackageImportResult;
import com.classification.domain_system.entity.*;
import com.classification.domain_system.exception.ResourceNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DomainPackageServiceTest {

    @Mock private DomainRepository domainRepository;
    @Mock private ClassificationAxisRepository axisRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private DqRuleRepository dqRuleRepository;
    @Mock private MatchingRuleRepository matchingRuleRepository;
    @Mock private WorkflowConfigRepository workflowConfigRepository;

    @InjectMocks
    private DomainPackageService domainPackageService;

    private UUID domainId;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "고객 도메인"));
        domain.setDescription(Map.of("ko", "고객 마스터 도메인"));
    }

    @Test
    @DisplayName("exportDomainPackage: 도메인 전체 메타데이터 패키지 정상 추출")
    void testExportDomainPackage() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));

        ClassificationAxis axis = new ClassificationAxis();
        axis.setAxisCode("MAIN");
        axis.setName(Map.of("ko", "기본 축"));
        when(axisRepository.findByDomainIdOrderBySortOrderAsc(domainId)).thenReturn(List.of(axis));

        ClassificationNode node = new ClassificationNode();
        node.setId(UUID.randomUUID());
        node.setName(Map.of("ko", "법인 고객"));
        node.setOrder(1);
        when(nodeRepository.findByDomain_Id(domainId)).thenReturn(List.of(node));

        FieldDefinition field = new FieldDefinition();
        field.setKey("biz_no");
        field.setName(Map.of("ko", "사업자번호"));
        field.setType("STRING");
        field.setRequired(true);
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(field));

        when(dqRuleRepository.findByDomainIdAndIsActiveTrueOrderBySortOrderAsc(domainId)).thenReturn(Collections.emptyList());
        when(matchingRuleRepository.findByDomainId(domainId)).thenReturn(Collections.emptyList());
        when(workflowConfigRepository.findByDomainId(domainId)).thenReturn(Collections.emptyList());

        DomainPackageDto pkg = domainPackageService.exportDomainPackage(domainId, "admin");

        assertThat(pkg).isNotNull();
        assertThat(pkg.getDomain().getName().get("ko")).isEqualTo("고객 도메인");
        assertThat(pkg.getAxes()).hasSize(1);
        assertThat(pkg.getNodes()).hasSize(1);
        assertThat(pkg.getFields()).hasSize(1);
        assertThat(pkg.getFields().get(0).getKey()).isEqualTo("biz_no");
    }

    @Test
    @DisplayName("exportDomainPackage: 존재하지 않는 도메인이면 ResourceNotFoundException 발생")
    void testExportNotFound() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> domainPackageService.exportDomainPackage(domainId, "admin"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Domain not found");
    }

    @Test
    @DisplayName("importDomainPackage: 패키지로부터 도메인 및 메타데이터 일괄 생성")
    void testImportDomainPackage() {
        when(domainRepository.findAll()).thenReturn(Collections.emptyList());
        when(domainRepository.save(any(Domain.class))).thenAnswer(i -> {
            Domain d = i.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        when(nodeRepository.save(any(ClassificationNode.class))).thenAnswer(i -> {
            ClassificationNode n = i.getArgument(0);
            if (n.getId() == null) n.setId(UUID.randomUUID());
            return n;
        });

        when(fieldDefinitionRepository.save(any(FieldDefinition.class))).thenAnswer(i -> {
            FieldDefinition f = i.getArgument(0);
            f.setId(UUID.randomUUID());
            return f;
        });

        DomainPackageDto pkg = DomainPackageDto.builder()
                .domain(DomainPackageDto.DomainInfo.builder().name(Map.of("ko", "신규 도메인")).description(Map.of("ko", "설명")).build())
                .nodes(List.of(
                        DomainPackageDto.NodeInfo.builder().nodeKey("N1").name(Map.of("ko", "루트 노드")).sortOrder(1).build()
                ))
                .fields(List.of(
                        DomainPackageDto.FieldInfo.builder().nodeKey("N1").key("name").name(Map.of("ko", "이름")).type("STRING").required(true).build()
                ))
                .build();

        DomainPackageImportResult result = domainPackageService.importDomainPackage(pkg, "admin", false);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDomainName()).isEqualTo("신규 도메인");
        assertThat(result.getNodeCount()).isEqualTo(1);
        assertThat(result.getFieldCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("importDomainPackage: 워크플로우 포함 패키지 임포트 시 actionType 정상 설정 및 DB 저장 검증")
    void testImportDomainPackage_WithWorkflows() {
        when(domainRepository.findAll()).thenReturn(Collections.emptyList());
        when(domainRepository.save(any(Domain.class))).thenAnswer(i -> {
            Domain d = i.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });
        when(workflowConfigRepository.save(any(WorkflowConfig.class))).thenAnswer(i -> {
            WorkflowConfig wf = i.getArgument(0);
            wf.setId(UUID.randomUUID());
            return wf;
        });

        DomainPackageDto pkg = DomainPackageDto.builder()
                .domain(DomainPackageDto.DomainInfo.builder().name(Map.of("ko", "결재 도메인")).build())
                .workflows(List.of(
                        DomainPackageDto.WorkflowInfo.builder()
                                .name("{\"ko\":\"등록 결재 서식\"}")
                                .actionType("CREATE")
                                .stepsConfig("{\"steps\":[]}")
                                .isDefault(true)
                                .isActive(true)
                                .build(),
                        DomainPackageDto.WorkflowInfo.builder()
                                .name("{\"ko\":\"스키마 변경 결재\"}")
                                .actionType("SCHEMA_CHANGE")
                                .stepsConfig("{\"steps\":[]}")
                                .isDefault(false)
                                .build(),
                        DomainPackageDto.WorkflowInfo.builder()
                                .name("{\"ko\":\"actionType 누락된 레거시 서식\"}")
                                .stepsConfig("{\"steps\":[]}")
                                .build()
                ))
                .build();

        DomainPackageImportResult result = domainPackageService.importDomainPackage(pkg, "admin", false);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getWorkflowCount()).isEqualTo(3);

        verify(workflowConfigRepository, times(3)).save(argThat(wf -> {
            assertThat(wf.getActionType()).isNotNull().isNotBlank();
            return true;
        }));
    }

    @Test
    @DisplayName("importDomainPackage: 기존 도메인에 동일 노드 및 필드가 존재할 때 중복 생성 없이 머지(Upsert) 검증")
    void testImportDomainPackage_MergeExistingNodesAndFields() {
        when(domainRepository.findAll()).thenReturn(List.of(domain));
        when(domainRepository.save(any(Domain.class))).thenReturn(domain);

        ClassificationNode existingNode = new ClassificationNode();
        UUID existingNodeId = UUID.randomUUID();
        existingNode.setId(existingNodeId);
        existingNode.setDomain(domain);
        existingNode.setName(Map.of("ko", "정규직"));
        existingNode.setOrder(1);
        when(nodeRepository.findByDomain_Id(domainId)).thenReturn(List.of(existingNode));
        when(nodeRepository.save(any(ClassificationNode.class))).thenAnswer(i -> i.getArgument(0));

        FieldDefinition existingField = new FieldDefinition();
        UUID existingFieldId = UUID.randomUUID();
        existingField.setId(existingFieldId);
        existingField.setDomain(domain);
        existingField.setDefinedAtNode(existingNode);
        existingField.setKey("EP_NO");
        existingField.setName(Map.of("ko", "사번"));
        existingField.setType("TEXT");
        existingField.setRequired(false);
        when(fieldDefinitionRepository.findByDomain_Id(domainId)).thenReturn(List.of(existingField));
        when(fieldDefinitionRepository.save(any(FieldDefinition.class))).thenAnswer(i -> i.getArgument(0));

        DomainPackageDto pkg = DomainPackageDto.builder()
                .domain(DomainPackageDto.DomainInfo.builder().name(Map.of("ko", "고객 도메인")).build())
                .nodes(List.of(
                        DomainPackageDto.NodeInfo.builder().nodeKey("N1").name(Map.of("ko", "정규직")).sortOrder(2).build()
                ))
                .fields(List.of(
                        DomainPackageDto.FieldInfo.builder().nodeKey("N1").key("EP_NO").name(Map.of("ko", "사원번호")).type("STRING").required(true).build()
                ))
                .build();

        DomainPackageImportResult result = domainPackageService.importDomainPackage(pkg, "admin", true);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getNodeCount()).isEqualTo(1);
        assertThat(result.getFieldCount()).isEqualTo(1);

        // 노드가 새로 생성되지 않고 기존 노드 ID를 유지한 채 머지되었는지 검증
        verify(nodeRepository, atLeastOnce()).save(argThat(n -> {
            return Objects.equals(n.getId(), existingNodeId);
        }));

        // 필드가 새로 생성되지 않고 기존 필드 ID를 유지한 채 속성이 업데이트되었는지 검증
        verify(fieldDefinitionRepository, atLeastOnce()).save(argThat(f -> {
            return Objects.equals(f.getId(), existingFieldId) && "STRING".equals(f.getType()) && Boolean.TRUE.equals(f.getRequired());
        }));
    }
}
