package com.classification.domain_system.service;

import com.classification.domain_system.dto.IntegrationChannelStatsDto;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.IntegrationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IntegrationChannelStatsTest {

    @Mock
    private IntegrationChannelRepository channelRepository;

    @Mock
    private IntegrationLogRepository logRepository;

    private IntegrationChannelService channelService;

    private UUID channelId1;
    private UUID channelId2;
    private IntegrationChannel channel1;
    private IntegrationChannel channel2;

    @BeforeEach
    void setUp() {
        channelService = new IntegrationChannelService(
                channelRepository,
                logRepository,
                null,
                null
        );

        channelId1 = UUID.randomUUID();
        channel1 = new IntegrationChannel();
        channel1.setId(channelId1);
        channel1.setName("카트봄 Outbound");
        channel1.setChannelCode("CARTBOM_OUT");
        channel1.setType("WEBHOOK");
        channel1.setActive(true);

        channelId2 = UUID.randomUUID();
        channel2 = new IntegrationChannel();
        channel2.setId(channelId2);
        channel2.setName("KRX Inbound");
        channel2.setChannelCode("KRX_IN");
        channel2.setType("DATABASE");
        channel2.setActive(true);
    }

    @Test
    @DisplayName("#147: 채널별 실행 로그 집계 및 성공률, SLA 헬스 상태(HEALTHY)가 정확히 산출된다")
    void testGetChannelStats_HealthyChannel() {
        when(channelRepository.findAll()).thenReturn(List.of(channel1));

        LocalDateTime now = LocalDateTime.now();
        IntegrationLog log1 = new IntegrationLog();
        log1.setId(UUID.randomUUID());
        log1.setChannelId(channelId1);
        log1.setStatus("SUCCESS");
        log1.setCreatedAt(now.minusMinutes(10));

        IntegrationLog log2 = new IntegrationLog();
        log2.setId(UUID.randomUUID());
        log2.setChannelId(channelId1);
        log2.setStatus("SUCCESS");
        log2.setCreatedAt(now.minusMinutes(5));

        when(logRepository.findByChannelId(eq(channelId1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log2, log1)));

        List<IntegrationChannelStatsDto> statsList = channelService.getAllChannelStats();
        assertThat(statsList).hasSize(1);

        IntegrationChannelStatsDto stats = statsList.get(0);
        assertThat(stats.getChannelId()).isEqualTo(channelId1);
        assertThat(stats.getChannelName()).isEqualTo("카트봄 Outbound");
        assertThat(stats.getTotalCount()).isEqualTo(2);
        assertThat(stats.getSuccessCount()).isEqualTo(2);
        assertThat(stats.getFailCount()).isEqualTo(0);
        assertThat(stats.getSuccessRate()).isEqualTo(100.0);
        assertThat(stats.getLastStatus()).isEqualTo("SUCCESS");
        assertThat(stats.getHealthStatus()).isEqualTo("HEALTHY");
    }

    @Test
    @DisplayName("#147: 실패 로그가 포함된 채널의 경우 CRITICAL 또는 WARNING 헬스 상태가 산출된다")
    void testGetChannelStats_FailingChannel() {
        when(channelRepository.findAll()).thenReturn(List.of(channel2));

        LocalDateTime now = LocalDateTime.now();
        IntegrationLog log1 = new IntegrationLog();
        log1.setId(UUID.randomUUID());
        log1.setChannelId(channelId2);
        log1.setStatus("FAIL");
        log1.setErrorMessage("502 Bad Gateway");
        log1.setCreatedAt(now.minusMinutes(1));

        IntegrationLog log2 = new IntegrationLog();
        log2.setId(UUID.randomUUID());
        log2.setChannelId(channelId2);
        log2.setStatus("SUCCESS");
        log2.setCreatedAt(now.minusHours(1));

        when(logRepository.findByChannelId(eq(channelId2), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log1, log2)));

        List<IntegrationChannelStatsDto> statsList = channelService.getAllChannelStats();
        assertThat(statsList).hasSize(1);

        IntegrationChannelStatsDto stats = statsList.get(0);
        assertThat(stats.getTotalCount()).isEqualTo(2);
        assertThat(stats.getSuccessCount()).isEqualTo(1);
        assertThat(stats.getFailCount()).isEqualTo(1);
        assertThat(stats.getSuccessRate()).isEqualTo(50.0);
        assertThat(stats.getLastStatus()).isEqualTo("FAIL");
        assertThat(stats.getHealthStatus()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("#147: 실행 이력이 없는 채널은 IDLE 상태로 안전하게 기본값을 반환한다")
    void testGetChannelStats_IdleChannel() {
        when(channelRepository.findAll()).thenReturn(List.of(channel1));
        when(logRepository.findByChannelId(eq(channelId1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<IntegrationChannelStatsDto> statsList = channelService.getAllChannelStats();
        assertThat(statsList).hasSize(1);

        IntegrationChannelStatsDto stats = statsList.get(0);
        assertThat(stats.getTotalCount()).isEqualTo(0);
        assertThat(stats.getSuccessRate()).isEqualTo(100.0);
        assertThat(stats.getLastStatus()).isEqualTo("IDLE");
        assertThat(stats.getHealthStatus()).isEqualTo("IDLE");
    }
}
