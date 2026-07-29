package com.classification.domain_system.integration;

import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.IntegrationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 연계 재시도 정책 및 Dead-Letter Queue(DLQ) 관리 서비스.
 * 지수 백오프(Exponential Backoff) 및 자동/수동 재시도를 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationRetryService {

    private final IntegrationLogRepository logRepository;
    private final IntegrationChannelRepository channelRepository;
    private final ApplicationContext applicationContext;

    /**
     * 채널 설정 및 현재 재시도 횟수를 기반으로 다음 재시도 예정 시각을 계산합니다.
     */
    public LocalDateTime calculateNextRetryTime(IntegrationChannel channel, int retryCount) {
        long backoffMs = channel != null ? channel.getRetryBackoffMs() : 1000L;
        boolean exponential = channel == null || channel.isUseExponentialBackoff();

        if (exponential) {
            // 2^retryCount 배율 적용 (최대 1시간 제한)
            long multiplier = (long) Math.pow(2, Math.min(retryCount, 10));
            backoffMs = Math.min(backoffMs * multiplier, 3600000L);
        }
        return LocalDateTime.now().plusNanos(backoffMs * 1_000_000L);
    }

    /**
     * 오류 로깅 시 재시도 횟수 초과 여부를 확인하고 status 및 nextRetryAt을 업데이트합니다.
     */
    public void updateLogRetryStatus(IntegrationLog integrationLog) {
        if (integrationLog == null || integrationLog.getChannelId() == null) return;

        IntegrationChannel channel = channelRepository.findById(integrationLog.getChannelId()).orElse(null);
        int maxRetries = channel != null ? channel.getMaxRetries() : 3;

        if (integrationLog.getRetryCount() >= maxRetries) {
            integrationLog.setStatus("DEAD_LETTER");
            integrationLog.setNextRetryAt(null);
            log.warn("[Integration DLQ] LogId={} exceeded maxRetries={}. Moved to DEAD_LETTER.",
                    integrationLog.getId(), maxRetries);
        } else {
            integrationLog.setStatus("FAIL");
            integrationLog.setNextRetryAt(calculateNextRetryTime(channel, integrationLog.getRetryCount()));
        }
    }

    /**
     * 지정된 백오프 시각이 지난 FAIL 건을 조회하여 자동 재시도를 처리합니다.
     */
    @Transactional
    public int processAutoRetries() {
        LocalDateTime now = LocalDateTime.now();
        List<IntegrationLog> overdueLogs = logRepository.findByStatusAndNextRetryAtBefore("FAIL", now);

        if (overdueLogs.isEmpty()) {
            return 0;
        }

        log.info("[Integration Retry] Found {} overdue failed integration logs for auto-retry", overdueLogs.size());
        int count = 0;

        for (IntegrationLog logEntity : overdueLogs) {
            try {
                executeRetry(logEntity);
                count++;
            } catch (Exception e) {
                log.error("[Integration Retry] Failed to retry logId={}: {}", logEntity.getId(), e.getMessage());
            }
        }
        return count;
    }

    /**
     * 특정 단일 연계 로그를 수동 재시도합니다. (DLQ건 포함)
     */
    @Transactional
    public void retrySingleLog(UUID logId) {
        IntegrationLog logEntity = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("IntegrationLog not found with id: " + logId));

        executeRetry(logEntity);
    }

    /**
     * DEAD_LETTER 상태의 모든 연계 로그를 일괄 수동 재시도합니다.
     */
    @Transactional
    public int retryAllDeadLetters() {
        List<IntegrationLog> deadLetters = logRepository.findByStatus("DEAD_LETTER");
        log.info("[Integration DLQ] Initiating bulk retry for {} dead-letter logs", deadLetters.size());

        int count = 0;
        for (IntegrationLog logEntity : deadLetters) {
            try {
                executeRetry(logEntity);
                count++;
            } catch (Exception e) {
                log.error("[Integration DLQ] Failed to retry logId={}: {}", logEntity.getId(), e.getMessage());
            }
        }
        return count;
    }

    /**
     * Dead-Letter Queue 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<IntegrationLog> getDeadLetters(int page, int size) {
        return logRepository.findByStatusOrderByCreatedAtDesc("DEAD_LETTER", PageRequest.of(page, size));
    }

    private void executeRetry(IntegrationLog logEntity) {
        IntegrationChannel channel = channelRepository.findById(logEntity.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("IntegrationChannel not found with id: " + logEntity.getChannelId()));

        org.springframework.messaging.Message<String> retryMessage = MessageBuilder
                .withPayload(logEntity.getOriginalPayload() != null ? logEntity.getOriginalPayload() : "")
                .setHeader("RECORD_ID", logEntity.getRecordId())
                .setHeader("EVENT_TYPE", logEntity.getEventType())
                .setHeader("CHANNEL_ID", channel.getId())
                .setHeader("CHANNEL_TYPE", channel.getType())
                .setHeader("MAPPING_CONFIG", channel.getMappingConfigJson())
                .setHeader("CHANNEL_CONFIG", channel.getConfigJson())
                .setHeader("ORIGINAL_PAYLOAD", logEntity.getOriginalPayload())
                .setHeader("IS_RETRY", true)
                .build();

        MessageChannel channelBean = applicationContext.getBean("integrationProcessingChannel", MessageChannel.class);
        channelBean.send(retryMessage);
        log.info("[Integration Retry] Dispatched retry message for logId={} channelId={}", logEntity.getId(), channel.getId());
    }
}
