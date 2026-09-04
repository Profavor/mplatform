package com.classification.domain_system.service;

import com.classification.domain_system.dto.CdcStreamDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CdcStreamingServiceTest {

    @Mock
    private RecordHistoryRepository recordHistoryRepository;

    private ObjectMapper objectMapper;
    private CdcStreamingService cdcStreamingService;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cdcStreamingService = new CdcStreamingService(recordHistoryRepository, objectMapper);
        domainId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getLiveCdcEvents: 실제 DB RecordHistory를 실시간 CDC 이벤트로 변환 검증")
    void testGetLiveCdcEventsFromRealHistory() {
        UUID recordId = UUID.randomUUID();
        UUID historyId = UUID.randomUUID();

        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "임직원", "en", "Employee"));

        ClassificationNode node = new ClassificationNode();
        node.setId(UUID.randomUUID());
        node.setDomain(domain);

        Record record = new Record();
        record.setId(recordId);
        record.setNode(node);
        record.setStatus("ACTIVE");

        RecordHistory history = new RecordHistory();
        history.setId(historyId);
        history.setRecordId(recordId);
        history.setRecord(record);
        history.setChangeType("UPDATE");
        history.setChangedBy("admin");
        history.setPreviousData("{\"EP_NO\": \"0000001\", \"EP_NAME\": \"인치국\"}");
        history.setNewData("{\"EP_NO\": \"0000001\", \"EP_NAME\": \"인치국\", \"status\": \"ACTIVE\"}");
        history.setSourceSystem("postgres-wal-cdc");
        history.setChangedAt(LocalDateTime.now());

        given(recordHistoryRepository.findRecentByDomainId(eq(domainId), any(Pageable.class)))
                .willReturn(List.of(history));

        CdcStreamDto.CdcStreamResponse res = cdcStreamingService.getLiveCdcEvents(domainId, null);

        assertThat(res).isNotNull();
        assertThat(res.getEvents()).hasSize(1);

        CdcStreamDto.CdcEventItem item = res.getEvents().get(0);
        assertThat(item.getEventId()).isEqualTo("CDC-" + historyId.toString().substring(0, 8));
        assertThat(item.getRecordCode()).isEqualTo("REC-" + recordId.toString().substring(0, 8));
        assertThat(item.getOperation()).isEqualTo("u");
        assertThat(item.getBeforePayload()).containsEntry("EP_NO", "0000001");
        assertThat(item.getAfterPayload()).containsEntry("status", "ACTIVE");
        assertThat(item.getSourceConnector()).isEqualTo("postgres-wal-cdc");
    }

    @Test
    @DisplayName("getLiveCdcEvents: 변경 이력이 없을 때 빈 응답 반환")
    void testGetLiveCdcEventsEmpty() {
        given(recordHistoryRepository.findRecentByDomainId(eq(domainId), any(Pageable.class)))
                .willReturn(List.of());

        CdcStreamDto.CdcStreamResponse res = cdcStreamingService.getLiveCdcEvents(domainId, null);

        assertThat(res).isNotNull();
        assertThat(res.getEvents()).isEmpty();
        assertThat(res.getActiveOffset()).isEqualTo(0L);
        assertThat(res.getEventsPerSecond()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getLiveCdcEvents: 특정 recordId가 지정되었을 때 해당 레코드의 변경 이력만 조회")
    void testGetLiveCdcEventsByRecordId() {
        UUID recordId = UUID.randomUUID();
        UUID historyId = UUID.randomUUID();

        RecordHistory history = new RecordHistory();
        history.setId(historyId);
        history.setRecordId(recordId);
        history.setChangeType("CREATE");
        history.setChangedBy("admin");
        history.setNewData("{\"EP_NO\": \"0000001\", \"EP_NAME\": \"인치국\"}");
        history.setChangedAt(LocalDateTime.now());

        given(recordHistoryRepository.findRecentByRecordId(eq(recordId), any(Pageable.class)))
                .willReturn(List.of(history));

        CdcStreamDto.CdcStreamResponse res = cdcStreamingService.getLiveCdcEvents(domainId, recordId);

        assertThat(res).isNotNull();
        assertThat(res.getEvents()).hasSize(1);
        assertThat(res.getEvents().get(0).getRecordCode()).isEqualTo("REC-" + recordId.toString().substring(0, 8));
        assertThat(res.getEvents().get(0).getOperation()).isEqualTo("c");
    }
}
