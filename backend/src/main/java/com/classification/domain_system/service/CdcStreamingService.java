package com.classification.domain_system.service;

import com.classification.domain_system.dto.CdcStreamDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CdcStreamingService {

    private final Map<UUID, List<CdcStreamDto.CdcEventItem>> domainEvents = new ConcurrentHashMap<>();

    public CdcStreamDto.CdcStreamResponse getLiveCdcEvents(UUID domainId) {
        List<CdcStreamDto.CdcEventItem> events = domainEvents.computeIfAbsent(domainId, k -> {
            List<CdcStreamDto.CdcEventItem> list = new ArrayList<>();
            list.add(CdcStreamDto.CdcEventItem.builder()
                    .eventId("CDC-EVT-001")
                    .timestamp(LocalDateTime.now().minusMinutes(2))
                    .operation("c")
                    .domainCode("DOM-CUST")
                    .recordCode("REC-001")
                    .sourceConnector("debezium-postgres-connector")
                    .beforePayload(null)
                    .afterPayload(Map.of("name", "홍길동", "grade", "VIP", "city", "서울"))
                    .build());

            list.add(CdcStreamDto.CdcEventItem.builder()
                    .eventId("CDC-EVT-002")
                    .timestamp(LocalDateTime.now().minusMinutes(1))
                    .operation("u")
                    .domainCode("DOM-CUST")
                    .recordCode("REC-001")
                    .sourceConnector("debezium-postgres-connector")
                    .beforePayload(Map.of("name", "홍길동", "grade", "VIP", "city", "서울"))
                    .afterPayload(Map.of("name", "홍길동", "grade", "VVIP", "city", "판교"))
                    .build());
            return list;
        });

        return CdcStreamDto.CdcStreamResponse.builder()
                .events(events)
                .activeOffset(events.size() * 1024L)
                .eventsPerSecond(12.8)
                .build();
    }

    public CdcStreamDto.CdcEventItem simulateCdcEvent(UUID domainId, CdcStreamDto.SimulateCdcRequest req) {
        getLiveCdcEvents(domainId); // ensure initialized
        List<CdcStreamDto.CdcEventItem> events = domainEvents.get(domainId);

        CdcStreamDto.CdcEventItem event = CdcStreamDto.CdcEventItem.builder()
                .eventId("CDC-EVT-" + String.format("%03d", events.size() + 1))
                .timestamp(LocalDateTime.now())
                .operation(req.getOperation() != null ? req.getOperation() : "u")
                .domainCode("DOM-CUST")
                .recordCode(req.getRecordCode() != null ? req.getRecordCode() : "REC-002")
                .sourceConnector("debezium-postgres-connector")
                .beforePayload(Map.of("status", "PENDING"))
                .afterPayload(req.getPayload() != null ? req.getPayload() : Map.of("status", "ACTIVE"))
                .build();

        events.add(0, event);
        return event;
    }
}
