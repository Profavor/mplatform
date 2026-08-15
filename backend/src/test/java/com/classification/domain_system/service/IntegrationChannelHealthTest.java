package com.classification.domain_system.service;

import com.classification.domain_system.dto.IntegrationMetricsDto;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.exception.ResourceNotFoundException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IntegrationChannelHealthTest {

    @Mock private IntegrationChannelRepository repository;
    @Mock private IntegrationLogRepository logRepository;
    @Mock private FieldEncryptionService encryptionService;

    @InjectMocks
    private IntegrationChannelService channelService;

    private UUID channelId;
    private IntegrationChannel channel;

    @BeforeEach
    void setUp() {
        channelId = UUID.randomUUID();
        channel = new IntegrationChannel();
        channel.setId(channelId);
        channel.setName("ERP Webhook Channel");
        channel.setType("WEB_SERVICE");
        channel.setActive(true);
    }

    @Test
    @DisplayName("getChannelMetrics: 24시간 로그 기반 처리량 및 헬스 상태 집계")
    void testGetChannelMetrics() {
        when(repository.findById(channelId)).thenReturn(Optional.of(channel));

        IntegrationLog log1 = new IntegrationLog();
        log1.setStatus("SUCCESS");
        log1.setCreatedAt(LocalDateTime.now().minusHours(2));

        IntegrationLog log2 = new IntegrationLog();
        log2.setStatus("SUCCESS");
        log2.setCreatedAt(LocalDateTime.now().minusHours(1));

        IntegrationLog log3 = new IntegrationLog();
        log3.setStatus("FAIL");
        log3.setCreatedAt(LocalDateTime.now().minusMinutes(30));

        when(logRepository.findByChannelIdAndCreatedAtAfter(eq(channelId), any(LocalDateTime.class)))
                .thenReturn(List.of(log1, log2, log3));

        IntegrationMetricsDto metrics = channelService.getChannelMetrics(channelId);

        assertThat(metrics).isNotNull();
        assertThat(metrics.getChannelName()).isEqualTo("ERP Webhook Channel");
        assertThat(metrics.getTotalRequests()).isEqualTo(3);
        assertThat(metrics.getSuccessCount()).isEqualTo(2);
        assertThat(metrics.getFailCount()).isEqualTo(1);
        assertThat(metrics.getDlqCount()).isEqualTo(0);
        assertThat(metrics.getHealthStatus()).isEqualTo("UNHEALTHY"); // 66.7% < 70%
        assertThat(metrics.getHourlyStats()).hasSize(24);
    }

    @Test
    @DisplayName("pingChannel: 실시간 핑 테스트 및 응답 시간 반환")
    void testPingChannel() {
        when(repository.findById(channelId)).thenReturn(Optional.of(channel));
        when(logRepository.findByChannelIdAndCreatedAtAfter(eq(channelId), any(LocalDateTime.class)))
                .thenReturn(List.of());

        IntegrationMetricsDto metrics = channelService.pingChannel(channelId);

        assertThat(metrics).isNotNull();
        assertThat(metrics.getLastPingLatencyMs()).isGreaterThan(0L);
        assertThat(metrics.getLastPingMessage()).contains("Ping check successful");
        assertThat(metrics.getLastPingAt()).isNotNull();
    }

    @Test
    @DisplayName("getChannelMetrics: 채널이 존재하지 않으면 ResourceNotFoundException 발생")
    void testGetMetricsNotFound() {
        when(repository.findById(channelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.getChannelMetrics(channelId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("IntegrationChannel not found");
    }
}
