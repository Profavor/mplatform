package com.classification.domain_system.service;

import com.classification.domain_system.dto.ClassificationNodeRequest;
import com.classification.domain_system.entity.ClassificationAxis;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.ClassificationAxisRepository;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassificationAxisNodeTreeTest {

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private ClassificationAxisRepository axisRepository;

    @InjectMocks
    private ClassificationNodeService nodeService;

    private UUID domainId;
    private UUID axis1Id;
    private UUID axis2Id;
    private Domain domain;
    private ClassificationAxis axis1;
    private ClassificationAxis axis2;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        axis1Id = UUID.randomUUID();
        axis2Id = UUID.randomUUID();

        domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "임직원 도메인"));

        axis1 = new ClassificationAxis();
        axis1.setId(axis1Id);
        axis1.setAxisCode("PLANT");
        axis1.setName(Map.of("ko", "플랜트 축"));

        axis2 = new ClassificationAxis();
        axis2.setId(axis2Id);
        axis2.setAxisCode("JOB");
        axis2.setName(Map.of("ko", "직군 축"));
    }

    @Test
    @DisplayName("특정 axisId를 지정하여 createNodeDirect를 실행하면 노드에 해당 axis가 세팅된다")
    void createNode_WithAxisId_SetsAxisOnNode() {
        ClassificationNodeRequest req = new ClassificationNodeRequest();
        req.setAxisId(axis1Id);
        req.setName(Map.of("ko", "울산공장"));

        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(axisRepository.findById(axis1Id)).thenReturn(Optional.of(axis1));
        when(nodeRepository.save(any(ClassificationNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClassificationNode result = nodeService.createNodeDirect(domainId, req);

        assertThat(result).isNotNull();
        assertThat(result.getAxis()).isEqualTo(axis1);
        assertThat(result.getAxisId()).isEqualTo(axis1Id);
    }

    @Test
    @DisplayName("axisId 파라미터가 주어진 getTree 호출 시 해당 축에 속한 독립 트리의 노드 목록만 반환된다")
    void getTree_WithAxisId_ReturnsNodesForSpecificAxis() {
        ClassificationNode nodeAxis1 = new ClassificationNode();
        nodeAxis1.setId(UUID.randomUUID());
        nodeAxis1.setDomain(domain);
        nodeAxis1.setAxis(axis1);
        nodeAxis1.setName(Map.of("ko", "울산공장"));

        when(nodeRepository.findByDomain_IdAndAxis_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId, axis1Id))
                .thenReturn(List.of(nodeAxis1));

        List<ClassificationNode> result = nodeService.getTree(domainId, axis1Id);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName().get("ko")).isEqualTo("울산공장");
        assertThat(result.get(0).getAxisId()).isEqualTo(axis1Id);
    }

    @Test
    @DisplayName("노드 삭제 성공 (Soft Delete flag 및 deletedAt 변경)")
    void deleteNode_Success() {
        UUID nodeId = UUID.randomUUID();
        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);
        node.setIsDeleted(false);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(nodeRepository.save(any(ClassificationNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        nodeService.deleteNode(domainId, nodeId);

        assertThat(node.getIsDeleted()).isTrue();
        assertThat(node.getDeletedAt()).isNotNull();
    }
}
