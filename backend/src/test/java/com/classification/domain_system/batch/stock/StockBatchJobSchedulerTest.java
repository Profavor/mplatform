package com.classification.domain_system.batch.stock;

import com.classification.domain_system.batch.stock.scheduler.StockBatchJobScheduler;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class StockBatchJobSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job stockMarketIngestionJob;

    private StockBatchJobScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new StockBatchJobScheduler(jobLauncher, stockMarketIngestionJob);
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
    @DisplayName("스케줄링 메소드 runDailyStockIngestion 정상 호출 검증")
    void runDailyStockIngestion_ShouldTriggerBatch() throws Exception {
        org.springframework.batch.core.job.JobInstance jobInstance = new org.springframework.batch.core.job.JobInstance(1L, "stockMarketIngestionJob");
        JobExecution mockExecution = new JobExecution(102L, jobInstance, new JobParameters());
        mockExecution.setStatus(BatchStatus.COMPLETED);

        given(jobLauncher.run(eq(stockMarketIngestionJob), any(JobParameters.class))).willReturn(mockExecution);

        scheduler.runDailyStockIngestion();

        verify(jobLauncher).run(eq(stockMarketIngestionJob), any(JobParameters.class));
    }
}
