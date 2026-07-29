package com.classification.domain_system.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 연계 실패 로그 중 백오프 시각이 경과된 건을 주기적으로 자동 재시도하는 스케줄러.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationRetryScheduler {

    private final IntegrationRetryService retryService;

    /**
     * 1분 간격으로 백오프 시간이 경과한 FAIL 연계 로그 자동 재시도
     */
    @Scheduled(cron = "${integration.retry.cron:0 * * * * ?}")
    public void runAutoRetries() {
        try {
            int retried = retryService.processAutoRetries();
            if (retried > 0) {
                log.info("[Integration Scheduler] Executed auto-retry for {} integration logs.", retried);
            }
        } catch (Exception e) {
            log.error("[Integration Scheduler] Error running auto-retry schedule: {}", e.getMessage(), e);
        }
    }
}
