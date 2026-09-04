package com.classification.domain_system.batch.stock.listener;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.integration.IntegrationLogService;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.websocket.WebSocketPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockJobExecutionListener implements JobExecutionListener {

    private final IntegrationChannelRepository channelRepository;
    private final Optional<IntegrationLogService> integrationLogService;
    private final Optional<WebSocketPublisher> webSocketPublisher;
    private final RecordRepository recordRepository;
    private final DomainRepository domainRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(">>> Starting Stock Market Spring Batch Job [ID: {}]", jobExecution.getId());

        String clearParam = jobExecution.getJobParameters().getString("clearExisting");
        if ("true".equalsIgnoreCase(clearParam)) {
            domainRepository.findBySpecializedCategory("STOCK").ifPresent(domain -> {
                List<Record> existing = recordRepository.findByNode_Domain_Id(domain.getId());
                if (existing != null && !existing.isEmpty()) {
                    for (Record r : existing) {
                        recordRepository.delete(r);
                    }
                    log.info("Safely deleted {} existing stock records before batch run.", existing.size());
                }
            });
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long writeCount = 0;
        long readCount = 0;
        long skipCount = 0;

        for (StepExecution step : jobExecution.getStepExecutions()) {
            writeCount += step.getWriteCount();
            readCount += step.getReadCount();
            skipCount += step.getSkipCount();
        }

        BatchStatus status = jobExecution.getStatus();
        log.info("<<< Stock Market Spring Batch Job finished with status [{}]. Read: {}, Written: {}, Skipped: {}",
                status, readCount, writeCount, skipCount);

        // Find Stock Inbound Channel
        IntegrationChannel channel = channelRepository.findAll().stream()
                .filter(ch -> ch.getName() != null && (ch.getName().contains("Stock") || ch.getName().contains("KRX")))
                .findFirst()
                .orElse(null);

        if (channel != null && integrationLogService.isPresent()) {
            try {
                if (status == BatchStatus.COMPLETED) {
                    integrationLogService.get().logSuccess(
                            channel.getId(),
                            null,
                            "SPRING_BATCH_STOCK_INGESTION",
                            String.format("Spring Batch Completed. Read: %d, Written: %d, Skipped: %d", readCount, writeCount, skipCount),
                            String.format("{\"readCount\":%d,\"writeCount\":%d,\"status\":\"%s\"}", readCount, writeCount, status)
                    );
                } else {
                    integrationLogService.get().logError(
                            channel.getId(),
                            null,
                            "SPRING_BATCH_STOCK_INGESTION",
                            "Batch Failed with status: " + status,
                            "{\"status\":\"" + status + "\"}",
                            "Batch failure: " + jobExecution.getAllFailureExceptions().toString(),
                            null,
                            0
                    );
                }
            } catch (Exception e) {
                log.warn("Could not write integration log for batch completion: {}", e.getMessage());
            }
        }

        // Send WebSocket notification
        if (webSocketPublisher.isPresent()) {
            try {
                webSocketPublisher.get().publishApprovalEvent(
                        "/topic/integration/batch-status",
                        Map.of(
                                "jobName", "stockMarketIngestionJob",
                                "status", status.name(),
                                "written", writeCount,
                                "read", readCount
                        )
                );
            } catch (Exception e) {
                log.debug("WebSocket broadcast failed: {}", e.getMessage());
            }
        }
    }
}
