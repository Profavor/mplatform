package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.MatchingRuleRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceOpenSearchFallbackTest {

    @Mock private MatchingRuleRepository matchingRuleRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private ElasticsearchOperations elasticsearchOperations;

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
    @DisplayName("OpenSearch에서 AbstractMethodError(getMatchedQueries) 발생 시 에러가 전파되지 않고 JPA DB fallback으로 정상 처리되어야 한다")
    void checkDuplicates_WhenOpenSearchThrowsAbstractMethodError_ShouldFallbackToJpaWithoutError() {
        UUID idFieldId = UUID.randomUUID();
        domain.setIdentifierFieldId(idFieldId);

        FieldDefinition idDef = new FieldDefinition();
        idDef.setId(idFieldId);
        idDef.setKey("MEMBER_ID");
        idDef.setName(Map.of("ko", "의원ID"));

        Record dup = new Record();
        dup.setId(UUID.randomUUID());

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of());
        when(fieldDefinitionRepository.findById(idFieldId)).thenReturn(Optional.of(idDef));

        // 시뮬레이션: OpenSearch SearchDocumentAdapter.getMatchedQueries() 미구현으로 인한 AbstractMethodError
        when(elasticsearchOperations.search(any(org.springframework.data.elasticsearch.core.query.Query.class), eq(com.classification.domain_system.entity.RecordDocument.class)))
                .thenThrow(new AbstractMethodError("Receiver class org.opensearch.data.client.orhlc.DocumentAdapters$SearchDocumentAdapter does not define or inherit an implementation of getMatchedQueries()"));

        // JPA fallback 모의
        when(recordRepository.findActiveRecordsByDomainAndFieldValue(domainId, "MEMBER_ID", "XBT9550Q"))
                .thenReturn(List.of(dup));

        // 검증: AbstractMethodError가 전파되지 않고 정상적으로 JPA Fallback 결과가 반환되어야 함
        assertThatCode(() -> {
            MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{\"MEMBER_ID\":\"XBT9550Q\"}");
            assertThat(result.hasDuplicates).isTrue();
            assertThat(result.duplicateRecordIds).containsExactly(dup.getId());
        }).doesNotThrowAnyException();
    }
}
