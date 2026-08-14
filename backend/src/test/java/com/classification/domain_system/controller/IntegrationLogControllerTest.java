package com.classification.domain_system.controller;

import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.integration.IntegrationLogService;
import com.classification.domain_system.integration.IntegrationRetryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationLogControllerTest {

    @Mock
    private IntegrationLogService logService;

    @Mock
    private IntegrationRetryService retryService;

    @InjectMocks
    private IntegrationLogController integrationLogController;

    private IntegrationLog createMockLog() {
        IntegrationLog log = new IntegrationLog();
        log.setId(UUID.randomUUID());
        log.setChannelId(UUID.randomUUID());
        log.setEventType("CREATE");
        log.setStatus("SUCCESS");
        return log;
    }

    @Test
    @DisplayName("연동 로그 목록 페이징 조회 성공")
    void testGetLogs_Success() {
        IntegrationLog log = createMockLog();
        Page<IntegrationLog> page = new PageImpl<>(List.of(log));

        when(logService.getLogs(any(), any(Pageable.class))).thenReturn(page);

        Page<IntegrationLog> result = integrationLogController.getLogs(null, 0, 50);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("SUCCESS", result.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("레코드 ID별 연동 로그 조회 성공")
    void testGetLogsByRecordId_Success() {
        UUID recordId = UUID.randomUUID();
        IntegrationLog log = createMockLog();
        log.setRecordId(recordId);

        when(logService.getLogsByRecordId(recordId)).thenReturn(List.of(log));

        List<IntegrationLog> result = integrationLogController.getLogsByRecordId(recordId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(recordId, result.get(0).getRecordId());
    }

    @Test
    @DisplayName("개별 실패 로그 재처리 요청 성공")
    void testRetryLog_Success() {
        UUID logId = UUID.randomUUID();

        ResponseEntity<Void> response = integrationLogController.retryLog(logId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(logService).retryLog(logId);
    }

    @Test
    @DisplayName("Dead-letter 큐 페이징 조회 성공")
    void testGetDeadLetters_Success() {
        IntegrationLog log = createMockLog();
        log.setStatus("DEAD_LETTER");
        Page<IntegrationLog> page = new PageImpl<>(List.of(log));

        when(retryService.getDeadLetters(0, 50)).thenReturn(page);

        PageResponse<IntegrationLog> response = integrationLogController.getDeadLetters(0, 50);

        assertNotNull(response);
        assertEquals(1, response.totalElements());
        assertEquals("DEAD_LETTER", response.content().get(0).getStatus());
    }

    @Test
    @DisplayName("전체 Dead-letter 일괄 재처리 성공")
    void testRetryAllDeadLetters_Success() {
        when(retryService.retryAllDeadLetters()).thenReturn(5);

        ResponseEntity<Map<String, Object>> response = integrationLogController.retryAllDeadLetters();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().get("retriedCount"));
        verify(retryService).retryAllDeadLetters();
    }
}
