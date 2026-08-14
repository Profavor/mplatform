package com.classification.domain_system.integration;

import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.repository.IntegrationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegrationLogService {

    private final IntegrationLogRepository logRepository;
    private final com.classification.domain_system.repository.IntegrationChannelRepository channelRepository;
    private final org.springframework.context.ApplicationContext applicationContext;
    private final IntegrationRetryService retryService;

    public org.springframework.data.domain.Page<IntegrationLog> getLogs(UUID channelId, org.springframework.data.domain.Pageable pageable) {
        if (channelId != null) {
            return logRepository.findByChannelId(channelId, pageable);
        }
        return logRepository.findAll(pageable);
    }

    public java.util.List<IntegrationLog> getLogsByRecordId(UUID recordId) {
        return logRepository.findByRecordIdOrderByCreatedAtDesc(recordId);
    }

    @Transactional
    public void logSuccess(UUID channelId, UUID recordId, String eventType, String originalPayload, String mappedPayload) {
        IntegrationLog log = new IntegrationLog();
        log.setChannelId(channelId);
        log.setRecordId(recordId);
        log.setEventType(eventType);
        log.setOriginalPayload(originalPayload);
        log.setMappedPayload(mappedPayload);
        log.setStatus("SUCCESS");
        logRepository.save(log);
    }

    @Transactional
    public void logError(UUID channelId, UUID recordId, String eventType, String originalPayload, String mappedPayload, String errorMessage, String stackTrace, int retryCount) {
        IntegrationLog log = new IntegrationLog();
        log.setChannelId(channelId);
        log.setRecordId(recordId);
        log.setEventType(eventType);
        log.setOriginalPayload(originalPayload);
        log.setMappedPayload(mappedPayload);
        log.setRetryCount(retryCount);
        
        // truncate error message if too long
        if (errorMessage != null && errorMessage.length() > 5000) {
            errorMessage = errorMessage.substring(0, 5000);
        }
        log.setErrorMessage(errorMessage);
        log.setStackTrace(stackTrace);

        // 채널 설정 기반으로 STATUS (FAIL vs DEAD_LETTER) 및 nextRetryAt 계산
        if (retryService != null) {
            retryService.updateLogRetryStatus(log);
        } else {
            log.setStatus("FAIL");
        }

        logRepository.save(log);
    }

    @Transactional
    public void retryLog(UUID logId) {
        if (retryService != null) {
            retryService.retrySingleLog(logId);
        }
    }
}
