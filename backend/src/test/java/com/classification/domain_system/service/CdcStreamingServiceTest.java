package com.classification.domain_system.service;

import com.classification.domain_system.dto.CdcStreamDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CdcStreamingServiceTest {

    private CdcStreamingService cdcStreamingService;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        cdcStreamingService = new CdcStreamingService();
        domainId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getLiveCdcEvents: 실시간 CDC 스트림 이벤트 목록 조회")
    void testGetLiveCdcEvents() {
        CdcStreamDto.CdcStreamResponse res = cdcStreamingService.getLiveCdcEvents(domainId);

        assertThat(res).isNotNull();
        assertThat(res.getEvents()).isNotEmpty();
        assertThat(res.getEvents().get(0).getEventId()).startsWith("CDC-EVT-");
        assertThat(res.getEvents().get(0).getSourceConnector()).contains("debezium");
    }

    @Test
    @DisplayName("simulateCdcEvent: 레코드 변경 CDC 이벤트 발행")
    void testSimulateCdcEvent() {
        CdcStreamDto.SimulateCdcRequest req = CdcStreamDto.SimulateCdcRequest.builder()
                .operation("u")
                .recordCode("REC-005")
                .payload(Map.of("status", "COMPLETED", "score", 95))
                .build();

        CdcStreamDto.CdcEventItem item = cdcStreamingService.simulateCdcEvent(domainId, req);

        assertThat(item).isNotNull();
        assertThat(item.getRecordCode()).isEqualTo("REC-005");
        assertThat(item.getAfterPayload()).containsEntry("status", "COMPLETED");

        CdcStreamDto.CdcStreamResponse res = cdcStreamingService.getLiveCdcEvents(domainId);
        assertThat(res.getEvents()).hasSizeGreaterThanOrEqualTo(3);
    }
}
