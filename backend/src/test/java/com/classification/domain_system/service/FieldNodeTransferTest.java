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

class FieldNodeTransferTest {

    private FieldDefinitionRepository fieldRepository;
    private ClassificationNodeRepository nodeRepository;
    private DomainRepository domainRepository;
    private FieldGroupRepository fieldGroupRepository;
    private FieldDefinitionService fieldService;

    @BeforeEach
    void setUp() {
        fieldRepository = Mockito.mock(FieldDefinitionRepository.class);
        nodeRepository = Mockito.mock(ClassificationNodeRepository.class);
        domainRepository = Mockito.mock(DomainRepository.class);
        fieldGroupRepository = Mockito.mock(FieldGroupRepository.class);

        fieldService = new FieldDefinitionService(
                fieldRepository,
                nodeRepository,
                domainRepository,
                fieldGroupRepository,
                null
        );
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
        when(nodeRepository.findById(oldNodeId)).thenReturn(Optional.of(oldNode));
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
}
