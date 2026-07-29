package com.classification.domain_system.controller;

import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.integration.IntegrationLogService;
import com.classification.domain_system.integration.IntegrationRetryService;
import com.classification.domain_system.repository.IntegrationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationLogControllerTest {

    @Mock
    private IntegrationLogRepository repository;

    @Mock
    private IntegrationLogService logService;

    @Mock
    private IntegrationRetryService retryService;

    @InjectMocks
    private IntegrationLogController controller;

    private UUID logId;

    @BeforeEach
    void setUp() {
        logId = UUID.randomUUID();
    }

    @Test
    @DisplayName("단일 연계 로그를 수동 재시도한다")
    void retryLog_Success() {
        ResponseEntity<Void> response = controller.retryLog(logId);

        verify(logService).retryLog(logId);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("Dead-Letter Queue 목록을 조회한다")
    void getDeadLetters_ReturnsPage() {
        IntegrationLog dlLog = new IntegrationLog();
        dlLog.setId(logId);
        dlLog.setStatus("DEAD_LETTER");

        when(retryService.getDeadLetters(anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of(dlLog)));

        var response = controller.getDeadLetters(0, 50);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).getStatus()).isEqualTo("DEAD_LETTER");
    }

    @Test
    @DisplayName("Dead-Letter Queue 일괄 재시도를 실행한다")
    void retryAllDeadLetters_Success() {
        when(retryService.retryAllDeadLetters()).thenReturn(5);

        ResponseEntity<Map<String, Object>> response = controller.retryAllDeadLetters();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("retriedCount", 5);
    }
}
