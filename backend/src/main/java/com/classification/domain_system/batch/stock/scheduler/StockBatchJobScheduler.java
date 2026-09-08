package com.classification.domain_system.batch.stock.scheduler;

import com.classification.domain_system.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockBatchJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job stockMarketIngestionJob;
    private final DistributedLockService distributedLockService;

    private static final String BATCH_LOCK_KEY = "scheduler:stockMarketIngestionJob";
    private static final Duration BATCH_LOCK_TIMEOUT = Duration.ofMinutes(15);

    /**
     * Default daily batch execution at 16:00 on weekdays (Mon-Fri) after Korean market close.
     */
    @Scheduled(cron = "${integration.stock.batch.cron:0 0 16 * * MON-FRI}")
    public void runDailyStockIngestion() {
        log.info("[StockBatchScheduler] Checking distributed lock before triggering scheduled daily stock market batch ingestion...");
        if (!distributedLockService.acquireLock(BATCH_LOCK_KEY, BATCH_LOCK_TIMEOUT)) {
            log.info("[StockBatchScheduler] Distributed lock is already held by another pod/instance. Skipping scheduled execution on this node.");
            return;
        }

        try {
            log.info("[StockBatchScheduler] Lock acquired. Triggering scheduled daily stock market batch ingestion...");
            runBatchJob(false, null, "SCHEDULED_CRON");
        } catch (Exception e) {
            log.error("[StockBatchScheduler] Error during scheduled stock batch execution: {}", e.getMessage(), e);
        }
    }

    /**
     * Executes the Spring Batch job with custom parameters.
     */
    public JobExecution runBatchJob(Boolean clearExisting, String markets, String requestedBy) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .addString("clearExisting", clearExisting != null ? clearExisting.toString() : "false")
                .addString("markets", markets != null ? markets : "")
                .addString("requestedBy", requestedBy != null ? requestedBy : "SPRING_BATCH")
                .toJobParameters();

        log.info("[StockBatchJobScheduler] Launching job 'stockMarketIngestionJob' with parameters: {}", jobParameters);
        return jobLauncher.run(stockMarketIngestionJob, jobParameters);
    }
}
