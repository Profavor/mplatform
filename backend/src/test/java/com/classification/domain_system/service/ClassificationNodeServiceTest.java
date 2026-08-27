package com.classification.domain_system.service;

import com.classification.domain_system.dto.ClassificationNodeRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.classification.domain_system.entity.ClassificationAxis;
import com.classification.domain_system.repository.ClassificationAxisRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassificationNodeServiceTest {

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private ClassificationAxisRepository axisRepository;

    @InjectMocks
    private ClassificationNodeService classificationNodeService;

    private UUID domainId;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "Test Domain", "en", "Test Domain"));
    }

    @Test
    void createNode_Success_RootNode() {
        ClassificationNodeRequest request = new ClassificationNodeRequest();
        request.setName(Map.of("ko", "Root Node", "en", "Root Node"));
        request.setOrder(1);

        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(nodeRepository.save(any(ClassificationNode.class))).thenAnswer(i -> {
            ClassificationNode node = i.getArgument(0);
            node.setId(UUID.randomUUID());
            return node;
        });

        ClassificationNode createdNode = classificationNodeService.createNode(domainId, request);

        assertThat(createdNode.getDepth()).isEqualTo(1);
        assertThat(createdNode.getPath()).isEqualTo("/Test Domain/Root Node");
        assertThat(createdNode.getParent()).isNull();
        verify(nodeRepository, times(1)).save(any(ClassificationNode.class));
    }

    @Test
    void createNode_Success_ChildNode() {
        UUID parentId = UUID.randomUUID();
        ClassificationNode parentNode = new ClassificationNode();
        parentNode.setId(parentId);
        parentNode.setDepth(1);
        parentNode.setPath("/Test Domain/Root Node");
        parentNode.setName(Map.of("ko", "Root Node"));

        ClassificationNodeRequest request = new ClassificationNodeRequest();
        request.setName(Map.of("ko", "Child Node"));
        request.setParentId(parentId);
        request.setOrder(1);

        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(nodeRepository.findById(parentId)).thenReturn(Optional.of(parentNode));
        when(nodeRepository.save(any(ClassificationNode.class))).thenAnswer(i -> {
            ClassificationNode node = i.getArgument(0);
            node.setId(UUID.randomUUID());
            return node;
        });

        ClassificationNode createdNode = classificationNodeService.createNode(domainId, request);

        assertThat(createdNode.getDepth()).isEqualTo(2);
        assertThat(createdNode.getPath()).isEqualTo("/Test Domain/Root Node/Child Node");
        assertThat(createdNode.getParent()).isEqualTo(parentNode);
    }

    @Test
    void createNode_ThrowsException_WhenDomainNotFound() {
        ClassificationNodeRequest request = new ClassificationNodeRequest();
        
        when(domainRepository.findById(domainId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> classificationNodeService.createNode(domainId, request));
    }

    @Test
    void getTree_Success() {
        ClassificationNode rootNode = new ClassificationNode();
        when(axisRepository.findByDomainIdAndIsDefaultTrue(domainId)).thenReturn(Optional.empty());
        when(nodeRepository.findByDomain_IdAndAxisIsNullAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId))
                .thenReturn(List.of(rootNode));

        List<ClassificationNode> tree = classificationNodeService.getTree(domainId);

        assertThat(tree).hasSize(1);
        verify(nodeRepository, times(1)).findByDomain_IdAndAxisIsNullAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId);
    }

    @Test
    void getTree_WithDefaultAxis_Success() {
        ClassificationAxis defaultAxis = new ClassificationAxis();
        UUID axisId = UUID.randomUUID();
        defaultAxis.setId(axisId);
        defaultAxis.setIsDefault(true);

        ClassificationNode rootNode = new ClassificationNode();
        when(axisRepository.findByDomainIdAndIsDefaultTrue(domainId)).thenReturn(Optional.of(defaultAxis));
        when(nodeRepository.findByDomain_IdAndAxis_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId, axisId))
                .thenReturn(List.of(rootNode));

        List<ClassificationNode> tree = classificationNodeService.getTree(domainId);

        assertThat(tree).hasSize(1);
        verify(nodeRepository, times(1)).findByDomain_IdAndAxis_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId, axisId);
    }
}
