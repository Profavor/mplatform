package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.MatchingRule;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.MatchingRuleRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock private MatchingRuleRepository matchingRuleRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;

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

    @Nested
    @DisplayName("checkDuplicates")
    class CheckDuplicates {

        @Test
        @DisplayName("nodeId가 존재하지 않으면 hasDuplicates=false 반환")
        void nodeNotFound_ReturnsFalse() {
            when(nodeRepository.findById(nodeId)).thenReturn(Optional.empty());

            MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{}");

            assertThat(result.hasDuplicates).isFalse();
        }

        @Test
        @DisplayName("Domain Identifier Field 기반 1단계에서 중복 발견 시 early-return")
        void identifierField_DuplicateFound_EarlyReturn() {
            UUID idFieldId = UUID.randomUUID();
            domain.setIdentifierFieldId(idFieldId);

            FieldDefinition idDef = new FieldDefinition();
            idDef.setId(idFieldId);
            idDef.setKey("ticker");
            idDef.setName(Map.of("ko", "종목코드"));

            Record dup = new Record();
            dup.setId(UUID.randomUUID());

            when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
            when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of());
            when(fieldDefinitionRepository.findById(idFieldId)).thenReturn(Optional.of(idDef));
            when(recordRepository.findDynamicRecords(anyList(), isNull(), anyMap(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(dup)));

            MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{\"ticker\":\"006800\"}");

            assertThat(result.hasDuplicates).isTrue();
            assertThat(result.duplicateRecordIds).containsExactly(dup.getId());
            assertThat(result.message).contains("Identifier Field");
        }

        @Test
        @DisplayName("identifier 필드 값이 비어 있으면 1단계 검사 스킵")
        void identifierField_EmptyValue_SkipCheck() {
            UUID idFieldId = UUID.randomUUID();
            domain.setIdentifierFieldId(idFieldId);

            FieldDefinition idDef = new FieldDefinition();
            idDef.setId(idFieldId);
            idDef.setKey("ticker");

            when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
            when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of());
            when(fieldDefinitionRepository.findById(idFieldId)).thenReturn(Optional.of(idDef));

            // ticker 값이 없는 JSON
            MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{\"name\":\"test\"}");

            assertThat(result.hasDuplicates).isFalse();
        }

        @Test
        @DisplayName("Custom Rule - nodeId=null 규칙은 모든 노드에 적용")
        void customRule_NullNodeId_AppliesToAllNodes() {
            domain.setIdentifierFieldId(null);

            MatchingRule rule = new MatchingRule();
            rule.setNodeId(null); // 전체 적용
            rule.setRuleName("테스트 규칙");
            rule.setTargetFieldKeys("[\"name\"]");

            Record dup = new Record();
            dup.setId(UUID.randomUUID());

            when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
            when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of(rule));
            when(recordRepository.findDynamicRecords(anyList(), isNull(), anyMap(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(dup)));

            MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{\"name\":\"홍길동\"}");

            assertThat(result.hasDuplicates).isTrue();
            assertThat(result.message).contains("테스트 규칙");
        }

        @Test
        @DisplayName("Custom Rule - 다른 nodeId 규칙은 해당 노드에서 skip")
        void customRule_DifferentNodeId_Skipped() {
            domain.setIdentifierFieldId(null);

            UUID otherNodeId = UUID.randomUUID();
            MatchingRule rule = new MatchingRule();
            rule.setNodeId(otherNodeId); // 다른 노드 전용
            rule.setRuleName("타 노드 규칙");
            rule.setTargetFieldKeys("[\"name\"]");

            when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
            when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of(rule));

            MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{\"name\":\"홍길동\"}");

            assertThat(result.hasDuplicates).isFalse();
        }

        @Test
        @DisplayName("Custom Rule - 규칙 필드 중 하나라도 누락 시 해당 규칙 skip")
        void customRule_MissingField_SkipsRule() {
            domain.setIdentifierFieldId(null);

            MatchingRule rule = new MatchingRule();
            rule.setNodeId(null);
            rule.setRuleName("복합 규칙");
            rule.setTargetFieldKeys("[\"name\",\"ticker\"]"); // ticker가 데이터에 없음

            when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
            when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of(rule));

            // ticker 없는 JSON
            MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{\"name\":\"홍길동\"}");

            assertThat(result.hasDuplicates).isFalse();
        }

        @Test
        @DisplayName("잘못된 JSON 입력 시 예외 없이 빈 DuplicateResult 반환")
        void invalidJson_ReturnsSafeResult() {
            when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
            when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of());

            MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "INVALID_JSON!!!");

            assertThat(result.hasDuplicates).isFalse();
            assertThat(result.duplicateRecordIds).isEmpty();
        }

        @Test
        @DisplayName("Custom Rule - FUZZY 매칭 규칙으로 유사한 레코드 탐지 시 matchType=FUZZY 및 score 반환")
        void customRule_FuzzyMatch_ReturnsFuzzyResult() {
            domain.setIdentifierFieldId(null);

            MatchingRule rule = new MatchingRule();
            rule.setId(UUID.randomUUID());
            rule.setNodeId(nodeId);
            rule.setRuleName("상호 유사도 검사");
            rule.setTargetFieldKeys("[\"name\"]");
            rule.setMatchType("FUZZY");
            rule.setSimilarityThreshold(0.80);

            Record cand = new Record();
            cand.setId(UUID.randomUUID());
            cand.setData("{\"name\":\"주식회사 삼성전자\"}");

            when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
            when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of(rule));
            when(recordRepository.findByNodeId(eq(nodeId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(cand)));

            // 유사한 명칭 ("(주)삼성전자")
            MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, "{\"name\":\"(주)삼성전자\"}");

            assertThat(result.hasDuplicates).isTrue();
            assertThat(result.matchType).isEqualTo("FUZZY");
            assertThat(result.score).isGreaterThanOrEqualTo(0.80);
            assertThat(result.duplicateRecordIds).containsExactly(cand.getId());
        }

        @Test
        @DisplayName("P2-1: FUZZY 매칭 시 recordRepository.findByNodeId에 PageRequest.of(0, 500) 상한이 전달된다")
        void customRule_FuzzyMatch_UsesPaginationLimit() {
            domain.setIdentifierFieldId(null);

            MatchingRule rule = new MatchingRule();
            rule.setId(UUID.randomUUID());
            rule.setNodeId(nodeId);
            rule.setRuleName("상호 유사도 검사");
            rule.setTargetFieldKeys("[\"name\"]");
            rule.setMatchType("FUZZY");
            rule.setSimilarityThreshold(0.80);

            Record cand = new Record();
            cand.setId(UUID.randomUUID());
            cand.setData("{\"name\":\"주식회사 삼성전자\"}");

            when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
            when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of(rule));

            org.mockito.ArgumentCaptor<Pageable> pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
            when(recordRepository.findByNodeId(eq(nodeId), pageableCaptor.capture()))
                    .thenReturn(new PageImpl<>(List.of(cand)));

            matchingService.checkDuplicates(nodeId, "{\"name\":\"(주)삼성전자\"}");

            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(500);
        }
    }
}

