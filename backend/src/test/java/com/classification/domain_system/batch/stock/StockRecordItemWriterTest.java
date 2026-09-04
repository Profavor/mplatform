package com.classification.domain_system.batch.stock;

import com.classification.domain_system.batch.stock.writer.StockRecordItemWriter;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.infrastructure.item.Chunk;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class StockRecordItemWriterTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RecordHistoryRepository recordHistoryRepository;

    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;

    private StockRecordItemWriter writer;
    private Domain testDomain;
    private ClassificationNode kospiNode;
    private FieldDefinition idField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testDomain = new Domain();
        testDomain.setId(UUID.randomUUID());
        testDomain.setName(Map.of("ko", "주식 종목 마스터"));

        idField = new FieldDefinition();
        idField.setId(UUID.randomUUID());
        idField.setKey("ticker_code");

        testDomain.setIdentifierFieldId(idField.getId());

        kospiNode = new ClassificationNode();
        kospiNode.setId(UUID.randomUUID());
        kospiNode.setName(Map.of("ko", "유가증권시장 (KOSPI)"));
        kospiNode.setDomain(testDomain);

        writer = new StockRecordItemWriter(recordRepository, recordHistoryRepository, fieldDefinitionRepository, "SPRING_BATCH");
    }

    @Test
    @DisplayName("기존에 동일한 ticker_code를 가진 레코드가 있으면 신규 생성하지 않고 머지(UPDATE)한다")
    void write_WhenMatchingRecordExists_ShouldMergeAndBumpVersion() throws Exception {
        given(fieldDefinitionRepository.findById(idField.getId())).willReturn(Optional.of(idField));

        Record existingRecord = new Record();
        existingRecord.setId(UUID.randomUUID());
        existingRecord.setNode(kospiNode);
        existingRecord.setStatus("ACTIVE");
        existingRecord.setData("{\"ticker_code\":\"005930\",\"stock_name\":\"삼성전자\",\"current_price\":75000}");
        existingRecord.setVersion(1);

        given(recordRepository.findActiveRecordsByDomainAndFieldValue(testDomain.getId(), "ticker_code", "005930"))
                .willReturn(List.of(existingRecord));
        given(recordRepository.save(any(Record.class))).willAnswer(invocation -> invocation.getArgument(0));

        Record incomingRecord = new Record();
        incomingRecord.setNode(kospiNode);
        incomingRecord.setStatus("ACTIVE");
        incomingRecord.setData("{\"ticker_code\":\"005930\",\"stock_name\":\"삼성전자\",\"current_price\":80000}");
        incomingRecord.setSearchableData("삼성전자 005930");

        writer.write(Chunk.of(incomingRecord));

        assertThat(existingRecord.getVersion()).isEqualTo(2);
        assertThat(existingRecord.getData()).contains("80000");

        verify(recordRepository, times(1)).save(existingRecord);
        verify(recordHistoryRepository, times(1)).save(argThat(h ->
                "BATCH_MERGE".equals(h.getChangeType()) && h.getVersion() == 2));
    }

    @Test
    @DisplayName("일치하는 레코드가 없으면 신규 레코드로 저장한다")
    void write_WhenNoMatchingRecord_ShouldInsertNewRecord() throws Exception {
        given(fieldDefinitionRepository.findById(idField.getId())).willReturn(Optional.of(idField));
        given(recordRepository.findActiveRecordsByDomainAndFieldValue(testDomain.getId(), "ticker_code", "034220"))
                .willReturn(List.of());
        given(recordRepository.save(any(Record.class))).willAnswer(invocation -> {
            Record r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        Record incomingRecord = new Record();
        incomingRecord.setNode(kospiNode);
        incomingRecord.setStatus("ACTIVE");
        incomingRecord.setData("{\"ticker_code\":\"034220\",\"stock_name\":\"LG디스플레이\",\"current_price\":12000}");
        incomingRecord.setVersion(1);

        writer.write(Chunk.of(incomingRecord));

        verify(recordRepository, times(1)).save(incomingRecord);
        verify(recordHistoryRepository, times(1)).save(argThat(h ->
                "BATCH_INGEST".equals(h.getChangeType())));
    }
}
