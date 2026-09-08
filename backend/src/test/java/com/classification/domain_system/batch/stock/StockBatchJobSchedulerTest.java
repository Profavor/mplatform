package com.classification.domain_system.batch.stock;

import com.classification.domain_system.batch.stock.scheduler.StockBatchJobScheduler;
import com.classification.domain_system.service.DistributedLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class StockBatchJobSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job stockMarketIngestionJob;

    @Mock
    private DistributedLockService distributedLockService;

    private StockBatchJobScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new StockBatchJobScheduler(jobLauncher, stockMarketIngestionJob, distributedLockService);
    }

    @Test
    @DisplayName("Spring Batch Job 실행 트리거 및 JobParameters 전달 검증")
    void runBatchJob_ShouldLaunchJobWithParameters() throws Exception {
        org.springframework.batch.core.job.JobInstance jobInstance = new org.springframework.batch.core.job.JobInstance(1L, "stockMarketIngestionJob");
        JobExecution mockExecution = new JobExecution(101L, jobInstance, new JobParameters());
        mockExecution.setStatus(BatchStatus.COMPLETED);

        given(jobLauncher.run(eq(stockMarketIngestionJob), any(JobParameters.class))).willReturn(mockExecution);

        JobExecution result = scheduler.runBatchJob(true, "KOSPI,KOSDAQ", "test_user");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verify(jobLauncher).run(eq(stockMarketIngestionJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("분산 락 획득 성공 시 스케줄링 메소드 runDailyStockIngestion 정상 실행 검증")
    void runDailyStockIngestion_WhenLockAcquired_ShouldTriggerBatch() throws Exception {
        given(distributedLockService.acquireLock(anyString(), any(Duration.class))).willReturn(true);

        org.springframework.batch.core.job.JobInstance jobInstance = new org.springframework.batch.core.job.JobInstance(1L, "stockMarketIngestionJob");
        JobExecution mockExecution = new JobExecution(102L, jobInstance, new JobParameters());
        mockExecution.setStatus(BatchStatus.COMPLETED);

        given(jobLauncher.run(eq(stockMarketIngestionJob), any(JobParameters.class))).willReturn(mockExecution);

        scheduler.runDailyStockIngestion();

        verify(distributedLockService).acquireLock(eq("scheduler:stockMarketIngestionJob"), any(Duration.class));
        verify(jobLauncher).run(eq(stockMarketIngestionJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("다른 인스턴스가 분산 락을 선점하여 획득 실패 시 스케줄링 메소드 실행 건너뛰기(Skip) 검증")
    void runDailyStockIngestion_WhenLockFailed_ShouldSkipBatch() throws Exception {
        given(distributedLockService.acquireLock(anyString(), any(Duration.class))).willReturn(false);

        scheduler.runDailyStockIngestion();

        verify(distributedLockService).acquireLock(eq("scheduler:stockMarketIngestionJob"), any(Duration.class));
        verify(jobLauncher, never()).run(any(Job.class), any(JobParameters.class));
    }
}
