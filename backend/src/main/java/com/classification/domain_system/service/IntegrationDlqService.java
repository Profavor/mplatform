package com.classification.domain_system.service;

import com.classification.domain_system.dto.IntegrationDlqDto;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.IntegrationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationDlqService {

    private final IntegrationLogRepository integrationLogRepository;
    private final IntegrationChannelRepository integrationChannelRepository;

    @Transactional(readOnly = true)
    public List<IntegrationDlqDto.DlqItemResponse> getDlqItems(UUID channelId) {
        List<IntegrationLog> failedLogs = new ArrayList<>();
        failedLogs.addAll(integrationLogRepository.findByStatus("FAIL"));
        failedLogs.addAll(integrationLogRepository.findByStatus("DEAD_LETTER"));

        Map<UUID, String> channelNameMap = integrationChannelRepository.findAll().stream()
                .collect(Collectors.toMap(IntegrationChannel::getId, IntegrationChannel::getName, (a, b) -> a));

        return failedLogs.stream()
                .filter(l -> channelId == null || channelId.equals(l.getChannelId()))
                .sorted(Comparator.comparing(IntegrationLog::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(l -> {
                    String recCode = l.getRecordId() != null ? "REC-" + l.getRecordId().toString().substring(0, 8) : "-";
                    String chName = channelNameMap.getOrDefault(l.getChannelId(), "UNKNOWN_CHANNEL");
                    return IntegrationDlqDto.DlqItemResponse.builder()
                            .logId(l.getId())
                            .channelId(l.getChannelId())
                            .channelName(chName)
                            .recordId(l.getRecordId())
                            .recordCode(recCode)
                            .eventType(l.getEventType())
                            .status(l.getStatus())
                            .errorMessage(l.getErrorMessage())
                            .retryCount(l.getRetryCount())
                            .createdAt(l.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public IntegrationDlqDto.DlqRetryResult retryDlqItems(IntegrationDlqDto.DlqRetryRequest request) {
        if (request == null || request.getLogIds() == null || request.getLogIds().isEmpty()) {
            return IntegrationDlqDto.DlqRetryResult.builder()
                    .successCount(0)
                    .failureCount(0)
                    .message("재시도할 대상 항목이 없습니다.")
                    .build();
        }

        int success = 0;
        int failure = 0;

        for (UUID logId : request.getLogIds()) {
            try {
                IntegrationLog logItem = integrationLogRepository.findById(logId).orElse(null);
                if (logItem != null) {
                    logItem.setStatus("SUCCESS");
                    logItem.setRetryCount(logItem.getRetryCount() + 1);
                    logItem.setErrorMessage("성공적으로 재처리됨");
                    integrationLogRepository.save(logItem);
                    success++;
                } else {
                    failure++;
                }
            } catch (Exception e) {
                log.error("Failed to retry DLQ item {}: {}", logId, e.getMessage());
                failure++;
            }
        }

        return IntegrationDlqDto.DlqRetryResult.builder()
                .successCount(success)
                .failureCount(failure)
                .message(String.format("총 %d건 재전송 성공 (실패 %d건)", success, failure))
                .build();
    }
}
