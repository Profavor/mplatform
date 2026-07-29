package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EffectiveFieldsCacheTest {

    @Mock
    private FieldDefinitionRepository fieldRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private FieldDefinitionService fieldDefinitionService;

    private UUID nodeId;
    private UUID domainId;
    private ClassificationNode mockNode;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
        domainId = UUID.randomUUID();

        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        mockNode = new ClassificationNode();
        mockNode.setId(nodeId);
        mockNode.setDomain(mockDomain);
    }

    @Test
    @DisplayName("getEffectiveFields 호출 시 리포지토리 조회가 수행되어 결과를 반환한다")
    void getEffectiveFields_ExecutesSuccessfully() {
        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(mockNode));
        when(fieldRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of());
        when(fieldRepository.findNodeFieldsWithSort(nodeId)).thenReturn(List.of());

        List<FieldDefinition> result = fieldDefinitionService.getEffectiveFields(nodeId);

        verify(nodeRepository, times(1)).findById(nodeId);
        assertThat(result).isNotNull();
    }
}
