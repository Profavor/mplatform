package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordBatchUpsertItemResult;
import com.classification.domain_system.dto.RecordBatchUpsertRequest;
import com.classification.domain_system.dto.RecordBatchUpsertResponse;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordBatchUpsertServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RecordHistoryRepository recordHistoryRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;

    @Mock
    private RecordService recordService;

    private ObjectMapper objectMapper;
    private RecordBatchUpsertService service;

    private UUID nodeId;
    private ClassificationNode node;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new RecordBatchUpsertService(
                recordRepository,
                recordHistoryRepository,
                nodeRepository,
                fieldDefinitionRepository,
                recordService,
                objectMapper
        );

        nodeId = UUID.randomUUID();
        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());

        node = new ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        lenient().when(recordService.processDataForSave(any(), any(), any())).thenAnswer(inv -> inv.getArgument(1));
        lenient().when(recordRepository.saveAllAndFlush(any())).thenAnswer(inv -> {
            Iterable<?> items = inv.getArgument(0);
            List<Record> result = new ArrayList<>();
            for (Object o : items) {
                if (o instanceof Record r) {
                    if (r.getId() == null) {
                        r.setId(UUID.randomUUID());
                    }
                    result.add(r);
                }
            }
            return result;
        });
    }

    @Test
    @DisplayName("신규 레코드 일괄 생성: DB에 존재하지 않는 PRODUCT_ID 배치 전송 시 CREATED로 정상 생성된다.")
    void batchUpsert_NewRecords_CreatedSuccessfully() {
        RecordBatchUpsertRequest.Item item1 = RecordBatchUpsertRequest.Item.builder()
                .data(Map.of("PRODUCT_ID", "PID-1001", "PRODUCT_NAME", "테스트 상품 1", "PRODUCT_PRICE", 15000))
                .build();

        RecordBatchUpsertRequest.Item item2 = RecordBatchUpsertRequest.Item.builder()
                .data("{\"PRODUCT_ID\": \"PID-1002\", \"PRODUCT_NAME\": \"테스트 상품 2\", \"PRODUCT_PRICE\": 25000}")
                .build();

        RecordBatchUpsertRequest request = RecordBatchUpsertRequest.builder()
                .businessKey("PRODUCT_ID")
                .autoApprove(true)
                .sourceSystem("cartbom")
                .records(List.of(item1, item2))
                .build();

        when(recordRepository.findActiveRecordsByNodeAndFieldValues(eq(nodeId), eq("PRODUCT_ID"), anyList()))
                .thenReturn(Collections.emptyList());

        RecordBatchUpsertResponse response = service.batchUpsertRecords(nodeId, request, "test-user");

        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getCreatedCount()).isEqualTo(2);
        assertThat(response.getUpdatedCount()).isEqualTo(0);
        assertThat(response.getFailedCount()).isEqualTo(0);

        verify(recordRepository, times(1)).saveAllAndFlush(argThat(coll -> ((Collection<Record>) coll).size() == 2));
        verify(recordHistoryRepository, times(1)).saveAllAndFlush(argThat(list -> ((List<RecordHistory>) list).size() == 2));
    }

    @Test
    @DisplayName("기존 레코드 수정 및 멱등성: 이미 존재하는 PRODUCT_ID 전송 시 UPDATED로 갱신되고 레코드 수가 증가하지 않는다.")
    void batchUpsert_ExistingRecord_UpdatedIdempotently() {
        UUID existingRecordId = UUID.randomUUID();
        Record existingRecord = new Record();
        existingRecord.setId(existingRecordId);
        existingRecord.setNode(node);
        existingRecord.setData("{\"PRODUCT_ID\": \"PID-1001\", \"PRODUCT_NAME\": \"이전 상품명\", \"PRODUCT_PRICE\": 10000}");
        existingRecord.setStatus("ACTIVE");

        RecordBatchUpsertRequest.Item item = RecordBatchUpsertRequest.Item.builder()
                .data(Map.of("PRODUCT_ID", "PID-1001", "PRODUCT_NAME", "갱신된 상품명", "PRODUCT_PRICE", 12000))
                .build();

        RecordBatchUpsertRequest request = RecordBatchUpsertRequest.builder()
                .businessKey("PRODUCT_ID")
                .records(List.of(item))
                .build();

        when(recordRepository.findActiveRecordsByNodeAndFieldValues(eq(nodeId), eq("PRODUCT_ID"), anyList()))
                .thenReturn(List.of(existingRecord));

        RecordBatchUpsertResponse response = service.batchUpsertRecords(nodeId, request, "cartbom-sync");

        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getCreatedCount()).isEqualTo(0);
        assertThat(response.getUpdatedCount()).isEqualTo(1);
        assertThat(response.getFailedCount()).isEqualTo(0);

        RecordBatchUpsertItemResult itemResult = response.getItems().get(0);
        assertThat(itemResult.getAction()).isEqualTo("UPDATED");
        assertThat(itemResult.getRecordId()).isEqualTo(existingRecordId);
        assertThat(existingRecord.getData()).contains("갱신된 상품명");
        assertThat(existingRecord.getData()).contains("12000");

        verify(recordRepository, times(1)).saveAllAndFlush(argThat(coll -> ((Collection<Record>) coll).size() == 1));
    }

    @Test
    @DisplayName("동일 배치 내 중복 PRODUCT_ID 멱등성: 한 배치에 동일 키가 2번 들어와도 1건만 생성 후 1건은 수정되어 중복 방지된다.")
    void batchUpsert_DuplicateRecordsInSameBatch_HandledIdempotently() {
        RecordBatchUpsertRequest.Item item1 = RecordBatchUpsertRequest.Item.builder()
                .data(Map.of("PRODUCT_ID", "PID-SAME", "PRODUCT_PRICE", 10000))
                .build();

        RecordBatchUpsertRequest.Item item2 = RecordBatchUpsertRequest.Item.builder()
                .data(Map.of("PRODUCT_ID", "PID-SAME", "PRODUCT_PRICE", 20000))
                .build();

        RecordBatchUpsertRequest request = RecordBatchUpsertRequest.builder()
                .businessKey("PRODUCT_ID")
                .records(List.of(item1, item2))
                .build();

        when(recordRepository.findActiveRecordsByNodeAndFieldValues(eq(nodeId), eq("PRODUCT_ID"), anyList()))
                .thenReturn(Collections.emptyList());

        RecordBatchUpsertResponse response = service.batchUpsertRecords(nodeId, request, "test-sync");

        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getCreatedCount()).isEqualTo(1);
        assertThat(response.getUpdatedCount()).isEqualTo(1);
        assertThat(response.getFailedCount()).isEqualTo(0);

        // 첫 번째는 CREATED, 두 번째는 UPDATED
        assertThat(response.getItems().get(0).getAction()).isEqualTo("CREATED");
        assertThat(response.getItems().get(1).getAction()).isEqualTo("UPDATED");
    }

    @Test
    @DisplayName("잘못된 형식 처리: 키 누락 또는 JSON 파싱 오류 항목은 FAILED 처리되고 나머지 정상 항목은 처리된다.")
    void batchUpsert_InvalidItem_RecordsFailureWithoutHaltingBatch() {
        RecordBatchUpsertRequest.Item validItem = RecordBatchUpsertRequest.Item.builder()
                .data(Map.of("PRODUCT_ID", "PID-VALID", "PRODUCT_PRICE", 10000))
                .build();

        RecordBatchUpsertRequest.Item invalidJsonItem = RecordBatchUpsertRequest.Item.builder()
                .data("{INVALID_JSON")
                .build();

        RecordBatchUpsertRequest.Item missingKeyItem = RecordBatchUpsertRequest.Item.builder()
                .data(Map.of("NO_KEY", 123))
                .build();

        RecordBatchUpsertRequest request = RecordBatchUpsertRequest.builder()
                .businessKey("PRODUCT_ID")
                .records(List.of(validItem, invalidJsonItem, missingKeyItem))
                .build();

        when(recordRepository.findActiveRecordsByNodeAndFieldValues(eq(nodeId), eq("PRODUCT_ID"), anyList()))
                .thenReturn(Collections.emptyList());

        RecordBatchUpsertResponse response = service.batchUpsertRecords(nodeId, request, "test-sync");

        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getCreatedCount()).isEqualTo(1);
        assertThat(response.getFailedCount()).isEqualTo(2);
        assertThat(response.getItems().get(1).getAction()).isEqualTo("FAILED");
        assertThat(response.getItems().get(2).getAction()).isEqualTo("FAILED");
    }
}
