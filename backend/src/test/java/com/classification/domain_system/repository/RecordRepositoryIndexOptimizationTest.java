package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.MatchingRuleRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.service.MatchingService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordRepositoryIndexOptimizationTest {

    @Mock
    private MatchingRuleRepository matchingRuleRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;

    @InjectMocks
    private MatchingService matchingService;

    private UUID nodeId;
    private UUID domainId;
    private ClassificationNode node;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        nodeId = UUID.randomUUID();

        domain = new Domain();
        domain.setId(domainId);

        node = new ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);
    }

    @Test
    @DisplayName("식별자 필드 조회 시 exact key로 먼저 1차 인덱스 조회를 수행한다")
    void identifierField_UsesExactKeyFirst() {
        UUID idFieldId = UUID.randomUUID();
        domain.setIdentifierFieldId(idFieldId);

        FieldDefinition idDef = new FieldDefinition();
        idDef.setId(idFieldId);
        idDef.setKey("BILL_NO");

        Record dup = new Record();
        dup.setId(UUID.randomUUID());

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of());
        when(fieldDefinitionRepository.findById(idFieldId)).thenReturn(Optional.of(idDef));
        when(recordRepository.findActiveRecordsByDomainAndFieldValue(domainId, "BILL_NO", "2207463"))
                .thenReturn(List.of(dup));

        MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{\"BILL_NO\":\"2207463\"}");

        assertThat(result.hasDuplicates).isTrue();
        assertThat(result.duplicateRecordIds).containsExactly(dup.getId());
        verify(recordRepository, times(1)).findActiveRecordsByDomainAndFieldValue(domainId, "BILL_NO", "2207463");
        verify(recordRepository, never()).findActiveRecordsByDomainAndFieldValue(domainId, "bill_no", "2207463");
    }

    @Test
    @DisplayName("exact key 결과가 없을 때 대소문자 fallback 조회가 안전하게 동작한다")
    void identifierField_FallbackToLowercaseKeyWhenNotFound() {
        UUID idFieldId = UUID.randomUUID();
        domain.setIdentifierFieldId(idFieldId);

        FieldDefinition idDef = new FieldDefinition();
        idDef.setId(idFieldId);
        idDef.setKey("BILL_NO");

        Record dup = new Record();
        dup.setId(UUID.randomUUID());

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of());
        when(fieldDefinitionRepository.findById(idFieldId)).thenReturn(Optional.of(idDef));
        
        // 1st attempt with "BILL_NO" returns empty
        when(recordRepository.findActiveRecordsByDomainAndFieldValue(domainId, "BILL_NO", "2207463"))
                .thenReturn(List.of());
        // 2nd fallback attempt with "bill_no" returns the record
        when(recordRepository.findActiveRecordsByDomainAndFieldValue(domainId, "bill_no", "2207463"))
                .thenReturn(List.of(dup));

        MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{\"BILL_NO\":\"2207463\"}");

        assertThat(result.hasDuplicates).isTrue();
        assertThat(result.duplicateRecordIds).containsExactly(dup.getId());
        verify(recordRepository, times(1)).findActiveRecordsByDomainAndFieldValue(domainId, "BILL_NO", "2207463");
        verify(recordRepository, times(1)).findActiveRecordsByDomainAndFieldValue(domainId, "bill_no", "2207463");
    }
}
