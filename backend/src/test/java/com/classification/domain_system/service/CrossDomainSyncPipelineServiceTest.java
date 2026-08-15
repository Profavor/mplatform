package com.classification.domain_system.service;

import com.classification.domain_system.dto.PipelineScheduleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CrossDomainSyncPipelineServiceTest {

    private CrossDomainSyncPipelineService pipelineService;

    @BeforeEach
    void setUp() {
        pipelineService = new CrossDomainSyncPipelineService();
    }

    @Test
    @DisplayName("createPipeline & getPipelines: 동기화 파이프라인 생성 및 조회")
    void testCreateAndGetPipelines() {
        PipelineScheduleDto.CreatePipelineRequest req = PipelineScheduleDto.CreatePipelineRequest.builder()
                .name("고객 도메인 -> 마케팅 도메인 동기화")
                .sourceDomainId(UUID.randomUUID())
                .targetDomainId(UUID.randomUUID())
                .cronExpression("0 0 3 * * ?")
                .build();

        PipelineScheduleDto.SyncPipelineItem item = pipelineService.createPipeline(req);

        assertThat(item).isNotNull();
        assertThat(item.getPipelineId()).startsWith("PIPE-");

        List<PipelineScheduleDto.SyncPipelineItem> list = pipelineService.getPipelines();
        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("triggerPipeline: 파이프라인 수동 즉시 실행")
    void testTriggerPipeline() {
        PipelineScheduleDto.PipelineTriggerResponse res = pipelineService.triggerPipeline("PIPE-001");

        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo("SUCCESS");
        assertThat(res.getSyncedCount()).isGreaterThan(0);
    }
}
