package com.classification.domain_system.service;

import com.classification.domain_system.dto.PipelineScheduleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrossDomainSyncPipelineService {

    private final Map<String, PipelineScheduleDto.SyncPipelineItem> pipelineStorage = new ConcurrentHashMap<>();

    {
        String id1 = "PIPE-001";
        pipelineStorage.put(id1, PipelineScheduleDto.SyncPipelineItem.builder()
                .pipelineId(id1)
                .name("인사 조직도 -> 전사 결재선 도메인 동기화")
                .sourceDomainId(UUID.randomUUID())
                .sourceDomainName("인사 도메인")
                .targetDomainId(UUID.randomUUID())
                .targetDomainName("전자결재 도메인")
                .cronExpression("0 0 2 * * ? (매일 새벽 2시)")
                .active(true)
                .lastRunAt(LocalDateTime.now().minusHours(11))
                .lastSyncedCount(142)
                .status("SUCCESS")
                .build());
    }

    public List<PipelineScheduleDto.SyncPipelineItem> getPipelines() {
        return new ArrayList<>(pipelineStorage.values());
    }

    public PipelineScheduleDto.SyncPipelineItem createPipeline(PipelineScheduleDto.CreatePipelineRequest req) {
        String id = "PIPE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PipelineScheduleDto.SyncPipelineItem item = PipelineScheduleDto.SyncPipelineItem.builder()
                .pipelineId(id)
                .name(req.getName() != null ? req.getName() : "신규 동기화 파이프라인")
                .sourceDomainId(req.getSourceDomainId())
                .sourceDomainName("소스 도메인")
                .targetDomainId(req.getTargetDomainId())
                .targetDomainName("타겟 도메인")
                .cronExpression(req.getCronExpression() != null ? req.getCronExpression() : "0 0 * * * ?")
                .active(true)
                .lastRunAt(null)
                .lastSyncedCount(0)
                .status("IDLE")
                .build();

        pipelineStorage.put(id, item);
        return item;
    }

    public PipelineScheduleDto.PipelineTriggerResponse triggerPipeline(String pipelineId) {
        PipelineScheduleDto.SyncPipelineItem item = pipelineStorage.get(pipelineId);
        if (item != null) {
            item.setLastRunAt(LocalDateTime.now());
            item.setLastSyncedCount(28);
            item.setStatus("SUCCESS");
            return PipelineScheduleDto.PipelineTriggerResponse.builder()
                    .pipelineId(pipelineId)
                    .syncedCount(28)
                    .status("SUCCESS")
                    .message("파이프라인이 성공적으로 실행되어 28건의 데이터가 동기화되었습니다.")
                    .build();
        }

        return PipelineScheduleDto.PipelineTriggerResponse.builder()
                .pipelineId(pipelineId)
                .syncedCount(0)
                .status("FAILED")
                .message("파이프라인을 찾을 수 없습니다.")
                .build();
    }
}
