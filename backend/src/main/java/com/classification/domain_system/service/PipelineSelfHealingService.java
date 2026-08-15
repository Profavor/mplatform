package com.classification.domain_system.service;

import com.classification.domain_system.dto.PipelineSelfHealingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineSelfHealingService {

    public PipelineSelfHealingDto.PipelineHealingReport getHealingReport() {
        List<PipelineSelfHealingDto.HealingActionLog> actions = new ArrayList<>();

        actions.add(PipelineSelfHealingDto.HealingActionLog.builder()
                .actionId("HEAL-001")
                .pipelineChannel("SAP ERP / 재무 마스터")
                .errorType("SCHEMA_MISMATCH")
                .diagnosedCause("신규 통화 코드 누락 -> ISO-4217 표준 포맷 자동 변환 매핑 적용")
                .healingStrategy("PAYLOAD_TRANSFORMATION")
                .recoveredCount(142)
                .status("AUTO_RESOLVED")
                .build());

        actions.add(PipelineSelfHealingDto.HealingActionLog.builder()
                .actionId("HEAL-002")
                .pipelineChannel("Salesforce / 고객 도메인")
                .errorType("NETWORK_TIMEOUT")
                .diagnosedCause("일시적 네트워크 지연 -> 지수 백오프 및 서킷 브레이커 자율 복구")
                .healingStrategy("BACKOFF_RETRY")
                .recoveredCount(88)
                .status("RECOVERED")
                .build());

        actions.add(PipelineSelfHealingDto.HealingActionLog.builder()
                .actionId("HEAL-003")
                .pipelineChannel("SCM Gateway / 협력사 도메인")
                .errorType("PAYLOAD_CORRUPTION")
                .diagnosedCause("B2B EDI 헤더 깨짐 -> 보조 Kafka 브로커로 트래픽 자율 우회")
                .healingStrategy("TRAFFIC_REROUTING")
                .recoveredCount(35)
                .status("AUTO_RESOLVED")
                .build());

        long resolved = actions.stream().filter(a -> "AUTO_RESOLVED".equals(a.getStatus()) || "RECOVERED".equals(a.getStatus())).count();
        double rate = actions.isEmpty() ? 100.0 : ((double) resolved / actions.size()) * 100.0;

        return PipelineSelfHealingDto.PipelineHealingReport.builder()
                .totalIncidents(actions.size())
                .autoHealedCount((int) resolved)
                .healingSuccessRate(rate)
                .actions(actions)
                .summary(String.format("AI 파이프라인 자율 복구 에이전트가 %d건의 장애를 100%% 자율 복구(총 265건 레코드 구제) 완료하였습니다.", actions.size()))
                .build();
    }

    public boolean triggerHealing(String channel) {
        log.info("Triggered AI self-healing for channel: {}", channel);
        return true;
    }
}
