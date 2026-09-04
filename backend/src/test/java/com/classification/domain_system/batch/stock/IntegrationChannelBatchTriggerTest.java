package com.classification.domain_system.batch.stock;

import com.classification.domain_system.batch.stock.scheduler.StockBatchJobScheduler;
import com.classification.domain_system.controller.IntegrationChannelController;
import com.classification.domain_system.service.IntegrationChannelService;
import com.classification.domain_system.service.IntegrationTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

class IntegrationChannelBatchTriggerTest {

    @Mock
    private IntegrationChannelService channelService;

    @Mock
    private IntegrationTestService testService;

    @Mock
    private StockBatchJobScheduler stockBatchJobScheduler;

    @Mock
    private Principal principal;

    private IntegrationChannelController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new IntegrationChannelController(channelService, testService, stockBatchJobScheduler);
        given(principal.getName()).willReturn("admin_tester");
    }

    @Test
    @DisplayName("POST /api/admin/integration/channels/{id}/trigger-batch 수동 배치 실행 성공 응답 검증")
    void triggerBatchJob_ShouldReturnSuccessResponse() throws Exception {
        UUID channelId = UUID.randomUUID();
        org.springframework.batch.core.job.JobInstance jobInstance = new org.springframework.batch.core.job.JobInstance(1L, "stockMarketIngestionJob");
        JobExecution execution = new JobExecution(999L, jobInstance, new JobParameters());
        execution.setStatus(BatchStatus.COMPLETED);

        given(stockBatchJobScheduler.runBatchJob(eq(true), eq("KOSPI"), eq("admin_tester")))
                .willReturn(execution);

        ResponseEntity<Map<String, Object>> response = controller.triggerBatchJob(
                channelId,
                Map.of("clearExisting", true, "markets", "KOSPI"),
                principal
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("jobExecutionId", 999L);
        assertThat(response.getBody()).containsEntry("status", "COMPLETED");
    }
}
