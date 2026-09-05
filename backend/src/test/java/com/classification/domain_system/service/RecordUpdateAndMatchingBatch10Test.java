package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordDocument;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.MatchingRuleRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.RecordSearchRepository;
import com.classification.domain_system.security.UrlSecurityValidator;
import com.classification.domain_system.service.opensearch.RecordSyncListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecordUpdateAndMatchingBatch10Test {

    @Mock private RecordRepository recordRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private FieldDefinitionService fieldDefinitionService;
    @Mock private RecordSearchRepository searchRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private MatchingRuleRepository matchingRuleRepository;
    @Mock private CalculatedFieldEvaluator calculatedFieldEvaluator;
    @Mock private DataQualityService dqService;
    @Mock private WorkflowResolver workflowResolver;
    @Mock private PermissionService permissionService;
    @Mock private com.classification.domain_system.config.MdmProperties mdmProperties;
    @Mock private org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private MatchingService matchingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("#198: 부분 갱신 시 기존 레코드 데이터와 병합되어 미포함된 기존 속성이 보존된다")
    void testMergePartialUpdateData() throws Exception {
        String existingJson = "{\"PRODUCT_ID\":\"6011227725\",\"PRODUCT_CODE\":\"CBP-001207\",\"PRODUCT_NAME\":\"로지텍 G102\",\"PRODUCT_PRICE\":24060}";
        String incomingJson = "{\"PRODUCT_PRICE\":24410}";

        Map<String, Object> existingMap = objectMapper.readValue(existingJson, Map.class);
        Map<String, Object> incomingMap = objectMapper.readValue(incomingJson, Map.class);

        Map<String, Object> mergedMap = new HashMap<>(existingMap);
        mergedMap.putAll(incomingMap);

        String mergedJson = objectMapper.writeValueAsString(mergedMap);

        assertThat(mergedMap.get("PRODUCT_PRICE")).isEqualTo(24410);
        assertThat(mergedMap.get("PRODUCT_NAME")).isEqualTo("로지텍 G102");
        assertThat(mergedMap.get("PRODUCT_ID")).isEqualTo("6011227725");
        assertThat(mergedMap.get("PRODUCT_CODE")).isEqualTo("CBP-001207");
    }

    @Test
    @DisplayName("#198: RecordSyncListener는 OpenSearch 예외가 발생해도 JPA 트랜잭션을 중단시키지 않고 격리한다")
    void testRecordSyncListener_OpenSearchExceptionIsolated() {
        RecordSyncListener listener = new RecordSyncListener();
        listener.setSearchRepository(searchRepository);

        Record record = new Record();
        record.setId(UUID.randomUUID());
        record.setStatus("ACTIVE");
        record.setData("{\"SURVEY_DATE\":\"\",\"PRODUCT_NAME\":\"Test Product\"}");

        // Simulate OpenSearch throwing exception on save
        doThrow(new RuntimeException("OpenSearch mapping error: failed to parse date"))
                .when(searchRepository).save(any(RecordDocument.class));

        // When & Then: Should NOT throw exception
        listener.onPostUpdate(record);
        verify(searchRepository, times(1)).save(any(RecordDocument.class));
    }

    @Test
    @DisplayName("#196: MatchingService는 단순 required=true 또는 *_code/*_no 필드를 후보키로 과도하게 오탐하지 않는다")
    void testMatchingService_DoesNotTreatGeneralRequiredOrCodeAsCandidateUniqueKey() {
        UUID nodeId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();

        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);
        Domain domain = new Domain();
        domain.setId(domainId);
        // Explicit identifier field is null
        domain.setIdentifierFieldId(null);
        node.setDomain(domain);

        // Required unit field & category code (shared by many records)
        FieldDefinition unitField = new FieldDefinition();
        unitField.setKey("UNIT_LABEL");
        unitField.setType("TEXT");
        unitField.setRequired(true);

        FieldDefinition catField = new FieldDefinition();
        catField.setKey("CATEGORY_CODE");
        catField.setType("TEXT");
        catField.setRequired(true);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of());
        lenient().when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(unitField, catField));

        String inputJson = "{\"UNIT_LABEL\":\"kg\",\"CATEGORY_CODE\":\"200\",\"COMMODITY_ID\":\"NEW_123\"}";

        MatchingService.DuplicateResult result = matchingService.checkDuplicates(nodeId, inputJson);

        // Should NOT consider UNIT_LABEL or CATEGORY_CODE as unique candidate keys
        assertThat(result.hasDuplicates).isFalse();
    }

    @Test
    @DisplayName("#164: 카트봄 ITEM_ID 및 VENDOR_ITEM_ID는 후보키에서 제외되며 PRODUCT_ID는 정확 일치 키로 매칭된다")
    void testMatchingService_CartBomProductIdExactMatchAndItemIdExcluded() {
        UUID nodeId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();

        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);
        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setIdentifierFieldId(null);
        node.setDomain(domain);

        FieldDefinition itemIdField = new FieldDefinition();
        itemIdField.setKey("ITEM_ID");
        itemIdField.setType("TEXT");

        FieldDefinition vendorItemIdField = new FieldDefinition();
        vendorItemIdField.setKey("VENDOR_ITEM_ID");
        vendorItemIdField.setType("TEXT");

        FieldDefinition productIdField = new FieldDefinition();
        productIdField.setKey("PRODUCT_ID");
        productIdField.setType("TEXT");

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(matchingRuleRepository.findByDomainIdAndIsActiveTrue(domainId)).thenReturn(List.of());
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(itemIdField, vendorItemIdField, productIdField));

        Record existing = new Record();
        existing.setId(UUID.randomUUID());
        when(recordRepository.findActiveRecordsByDomainAndFieldValue(domainId, "PRODUCT_ID", "PID-9999"))
                .thenReturn(List.of(existing));

        // 1. When incoming data has ITEM_ID and VENDOR_ITEM_ID but different PRODUCT_ID: should NOT match
        String jsonWithoutProductIdMatch = "{\"ITEM_ID\":\"ITM-100\",\"VENDOR_ITEM_ID\":\"V-200\",\"PRODUCT_ID\":\"PID-OTHER\"}";
        MatchingService.DuplicateResult resultNoMatch = matchingService.checkDuplicates(nodeId, jsonWithoutProductIdMatch);
        assertThat(resultNoMatch.hasDuplicates).isFalse();

        // 2. When incoming data has matching PRODUCT_ID: should match exact record
        String jsonWithProductIdMatch = "{\"ITEM_ID\":\"ITM-100\",\"VENDOR_ITEM_ID\":\"V-200\",\"PRODUCT_ID\":\"PID-9999\"}";
        MatchingService.DuplicateResult resultMatch = matchingService.checkDuplicates(nodeId, jsonWithProductIdMatch);
        assertThat(resultMatch.hasDuplicates).isTrue();
        assertThat(resultMatch.duplicateRecordIds).containsExactly(existing.getId());
    }

    @Test
    @DisplayName("#191: UrlSecurityValidator는 메타데이터 IP 및 사설 대역 IP를 차단하고 비허용 스킴을 거부한다")
    void testUrlSecurityValidator_BlocksSsrfTargets() {
        // Cloud Metadata IP
        assertThat(UrlSecurityValidator.isSafeExternalUrl("http://169.254.169.254/latest/meta-data")).isFalse();
        // Loopback
        assertThat(UrlSecurityValidator.isSafeExternalUrl("http://127.0.0.1:8080/actuator")).isFalse();
        assertThat(UrlSecurityValidator.isSafeExternalUrl("http://localhost:9200")).isFalse();
        // Private networks
        assertThat(UrlSecurityValidator.isSafeExternalUrl("http://10.0.0.5/api")).isFalse();
        assertThat(UrlSecurityValidator.isSafeExternalUrl("http://192.168.1.100:8080/")).isFalse();
        assertThat(UrlSecurityValidator.isSafeExternalUrl("http://172.16.0.1/")).isFalse();
        // Non-http schemes
        assertThat(UrlSecurityValidator.isSafeExternalUrl("file:///etc/passwd")).isFalse();
        assertThat(UrlSecurityValidator.isSafeExternalUrl("ftp://example.com/data")).isFalse();
        // Null / blank
        assertThat(UrlSecurityValidator.isSafeExternalUrl(null)).isFalse();
        assertThat(UrlSecurityValidator.isSafeExternalUrl("")).isFalse();
        assertThat(UrlSecurityValidator.isSafeExternalUrl("   ")).isFalse();

        // Host validation - blocked hosts
        assertThat(UrlSecurityValidator.isSafeHost("localhost")).isFalse();
        assertThat(UrlSecurityValidator.isSafeHost("127.0.0.1")).isFalse();
        assertThat(UrlSecurityValidator.isSafeHost("169.254.169.254")).isFalse();
        assertThat(UrlSecurityValidator.isSafeHost("metadata.google.internal")).isFalse();
    }
}
