package com.classification.domain_system.service;

import com.classification.domain_system.dto.IntegrationDlqDto;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.IntegrationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IntegrationDlqServiceTest {

    @Mock private IntegrationLogRepository integrationLogRepository;
    @Mock private IntegrationChannelRepository integrationChannelRepository;

    @InjectMocks
    private IntegrationDlqService integrationDlqService;

    private UUID channelId;
    private UUID logId;
    private IntegrationLog failedLog;
    private IntegrationChannel channel;

    @BeforeEach
    void setUp() {
        channelId = UUID.randomUUID();
        logId = UUID.randomUUID();

        channel = new IntegrationChannel();
        channel.setId(channelId);
        channel.setName("ERP 인사 시스템 연계");

        failedLog = new IntegrationLog();
        failedLog.setId(logId);
        failedLog.setChannelId(channelId);
        failedLog.setStatus("FAIL");
        failedLog.setErrorMessage("Connection timeout (504)");
        failedLog.setCreatedAt(LocalDateTime.now());
        failedLog.setRetryCount(1);
    }

    @Test
    @DisplayName("getDlqItems: 실패 큐 목록 및 채널명 정상 매핑")
    void testGetDlqItems() {
        when(integrationLogRepository.findByStatus("FAIL")).thenReturn(List.of(failedLog));
        when(integrationLogRepository.findByStatus("DEAD_LETTER")).thenReturn(Collections.emptyList());
        when(integrationChannelRepository.findAll()).thenReturn(List.of(channel));

        List<IntegrationDlqDto.DlqItemResponse> items = integrationDlqService.getDlqItems(null);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getChannelName()).isEqualTo("ERP 인사 시스템 연계");
        assertThat(items.get(0).getStatus()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("retryDlqItems: DLQ 재처리 시 SUCCESS 갱신 및 재시도 카운트 증가")
    void testRetryDlqItems() {
        when(integrationLogRepository.findById(logId)).thenReturn(Optional.of(failedLog));

        IntegrationDlqDto.DlqRetryRequest request = IntegrationDlqDto.DlqRetryRequest.builder()
                .logIds(List.of(logId))
                .build();

        IntegrationDlqDto.DlqRetryResult result = integrationDlqService.retryDlqItems(request);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(failedLog.getStatus()).isEqualTo("SUCCESS");
        assertThat(failedLog.getRetryCount()).isEqualTo(2);
        verify(integrationLogRepository, times(1)).save(failedLog);
    }
}
