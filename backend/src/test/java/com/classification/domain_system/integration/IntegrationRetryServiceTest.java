package com.classification.domain_system.integration;

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
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationRetryServiceTest {

    @Mock
    private IntegrationLogRepository logRepository;

    @Mock
    private IntegrationChannelRepository channelRepository;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private IntegrationRetryService retryService;

    private UUID channelId;
    private UUID logId;
    private IntegrationChannel channel;
    private IntegrationLog log;

    @BeforeEach
    void setUp() {
        channelId = UUID.randomUUID();
        logId = UUID.randomUUID();

        channel = new IntegrationChannel();
        channel.setId(channelId);
        channel.setMaxRetries(3);
        channel.setRetryBackoffMs(1000L);
        channel.setUseExponentialBackoff(true);

        log = new IntegrationLog();
        log.setId(logId);
        log.setChannelId(channelId);
        log.setOriginalPayload("{\"key\":\"val\"}");
        log.setStatus("FAIL");
        log.setRetryCount(1);
    }

    @Test
    @DisplayName("지수 백오프 계산 시 2^retryCount 배율로 다음 재시도 시각을 계산한다")
    void calculateNextRetryTime_ExponentialBackoff() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRetry = retryService.calculateNextRetryTime(channel, 2);

        // retryCount=2일 때 1000ms * (2^2) = 4000ms = 4초 후
        assertThat(nextRetry).isAfterOrEqualTo(now.plusSeconds(3));
    }

    @Test
    @DisplayName("최대 재시도 횟수를 초과한 경우 DEAD_LETTER 상태로 변경된다")
    void logError_ExceedsMaxRetries_SetsDeadLetter() {
        IntegrationLog newLog = new IntegrationLog();
        newLog.setChannelId(channelId);
        newLog.setStatus("FAIL");
        newLog.setRetryCount(3); // maxRetries=3과 동일

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(channel));

        retryService.updateLogRetryStatus(newLog);

        assertThat(newLog.getStatus()).isEqualTo("DEAD_LETTER");
        assertThat(newLog.getNextRetryAt()).isNull();
    }

    @Test
    @DisplayName("자동 재시도 시 백오프 시각이 지난 FAIL 건만 재시도를 수행한다")
    void processAutoRetries_ExecutesOverdueFailures() {
        IntegrationLog overdueLog = new IntegrationLog();
        overdueLog.setId(UUID.randomUUID());
        overdueLog.setChannelId(channelId);
        overdueLog.setOriginalPayload("{}");
        overdueLog.setStatus("FAIL");
        overdueLog.setNextRetryAt(LocalDateTime.now().minusMinutes(1));

        when(logRepository.findByStatusAndNextRetryAtBefore(eq("FAIL"), any())).thenReturn(List.of(overdueLog));
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(applicationContext.getBean("integrationProcessingChannel", MessageChannel.class)).thenReturn(messageChannel);

        int processed = retryService.processAutoRetries();

        assertThat(processed).isEqualTo(1);
        verify(messageChannel).send(any());
    }
}
