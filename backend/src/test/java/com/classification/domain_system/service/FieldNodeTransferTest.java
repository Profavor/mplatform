package com.classification.domain_system.service;

import com.classification.domain_system.dto.FieldDefinitionRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.FieldGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import com.classification.domain_system.security.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class FieldNodeTransferTest {

    @Mock private FieldDefinitionRepository fieldRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private FieldGroupRepository fieldGroupRepository;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private FieldDefinitionService fieldService;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(securityUtils.getCurrentUserId()).thenReturn("tester");
    }

    @Test
    @DisplayName("필드 수정 시 targetNodeId 전달 시 해당 분류 노드로 소속 노드가 변경되는지 검증")
    void testFieldTransferToNewNode() {
        UUID oldNodeId = UUID.randomUUID();
        UUID newNodeId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();

        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());

        ClassificationNode oldNode = new ClassificationNode();
        oldNode.setId(oldNodeId);
        oldNode.setDomain(domain);

        ClassificationNode newNode = new ClassificationNode();
        newNode.setId(newNodeId);
        newNode.setDomain(domain);

        FieldDefinition field = new FieldDefinition();
        field.setId(fieldId);
        field.setDefinedAtNode(oldNode);
        field.setKey("test_field");

        FieldDefinitionRequest request = new FieldDefinitionRequest();
        request.setKey("test_field");
        request.setTargetNodeId(newNodeId);

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(nodeRepository.findById(newNodeId)).thenReturn(Optional.of(newNode));
        when(fieldRepository.save(any(FieldDefinition.class))).thenAnswer(i -> i.getArgument(0));

        FieldDefinition updated = fieldService.updateFieldDirect(oldNodeId, fieldId, request);

        assertNotNull(updated.getDefinedAtNode());
        assertEquals(newNodeId, updated.getDefinedAtNode().getId());
    }

    @Test
    @DisplayName("필드 수정 시 isDomainField가 true인 경우 도메인 공통 필드로 변경되는지 검증")
    void testFieldTransferToDomainLevel() {
        UUID oldNodeId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();

        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());

        ClassificationNode oldNode = new ClassificationNode();
        oldNode.setId(oldNodeId);
        oldNode.setDomain(domain);

        FieldDefinition field = new FieldDefinition();
        field.setId(fieldId);
        field.setDefinedAtNode(oldNode);
        field.setKey("test_field");

        FieldDefinitionRequest request = new FieldDefinitionRequest();
        request.setKey("test_field");
        request.setIsDomainField(true);

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(fieldRepository.save(any(FieldDefinition.class))).thenAnswer(i -> i.getArgument(0));

        FieldDefinition updated = fieldService.updateFieldDirect(oldNodeId, fieldId, request);

        assertNull(updated.getDefinedAtNode());
        assertNotNull(updated.getDomain());
        assertEquals(domain.getId(), updated.getDomain().getId());
    }

    @Test
    @DisplayName("노드 레벨 필드 생성 중 isDomainField가 true인 경우 도메인 레벨 필드로 생성되는지 검증")
    void testCreateFieldWithIsDomainFieldTrue() {
        UUID nodeId = UUID.randomUUID();
        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());

        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);

        FieldDefinitionRequest request = new FieldDefinitionRequest();
        request.setKey("domain_field_key");
        request.setIsDomainField(true);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(fieldRepository.save(any(FieldDefinition.class))).thenAnswer(i -> i.getArgument(0));

        FieldDefinition created = fieldService.addFieldDirect(nodeId, request);

        assertNull(created.getDefinedAtNode());
        assertNotNull(created.getDomain());
        assertEquals(domain.getId(), created.getDomain().getId());
    }

    @Test
    @DisplayName("노드 레벨 필드 생성 중 targetNodeId가 다른 분류 노드로 지정된 경우 해당 타겟 노드로 생성되는지 검증")
    void testCreateFieldWithTargetNodeId() {
        UUID nodeId = UUID.randomUUID();
        UUID targetNodeId = UUID.randomUUID();
        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());

        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);

        ClassificationNode targetNode = new ClassificationNode();
        targetNode.setId(targetNodeId);
        targetNode.setDomain(domain);

        FieldDefinitionRequest request = new FieldDefinitionRequest();
        request.setKey("node_field_key");
        request.setTargetNodeId(targetNodeId);
        request.setIsDomainField(false);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(nodeRepository.findById(targetNodeId)).thenReturn(Optional.of(targetNode));
        when(fieldRepository.save(any(FieldDefinition.class))).thenAnswer(i -> i.getArgument(0));

        FieldDefinition created = fieldService.addFieldDirect(nodeId, request);

        assertNotNull(created.getDefinedAtNode());
        assertEquals(targetNodeId, created.getDefinedAtNode().getId());
        assertNull(created.getDomain());
    }
}

