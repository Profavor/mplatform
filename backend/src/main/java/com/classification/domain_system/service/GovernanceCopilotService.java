package com.classification.domain_system.service;

import com.classification.domain_system.dto.GovernanceCopilotDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GovernanceCopilotService {

    public GovernanceCopilotDto.CopilotChatResponse askCopilot(GovernanceCopilotDto.CopilotChatRequest request) {
        String prompt = request.getPrompt() != null ? request.getPrompt().toLowerCase() : "";
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        String reply;
        List<String> suggestedActions;
        Map<String, String> metricCards;

        if (prompt.contains("품질") || prompt.contains("dq") || prompt.contains("quality")) {
            reply = "전사 마스터 데이터 품질 종합 점수는 **96.8점 (Level 4: 정량적 통제)**입니다. 특히 고객 도메인의 완전성(99.2%) 및 적시성(99.8%)이 우수하며, AI 이상치 자율 정제 엔진이 활성화되어 있습니다.";
            suggestedActions = List.of("AI 자율 정제 검토", "품질 KPI 대시보드 열기", "골든 레코드 병합 현황");
            metricCards = Map.of("전사 DQ 점수", "96.8점", "완전성 지표", "99.2%", "자율 정제율", "100%");
        } else if (prompt.contains("sla") || prompt.contains("지연") || prompt.contains("latency")) {
            reply = "현재 전사 3개 데이터 서비스 수준 협약(SLA)이 **100% 정상 준수** 중입니다. ERP 연계(32ms), CRM 고객 스트림(18ms) 등 평균 응답속도는 56.6ms로 목표치(<100ms)를 대폭 상회하고 있습니다.";
            suggestedActions = List.of("SLA 협약 상세 보기", "트래픽 레이더 확인", "채널 헬스체인 진단");
            metricCards = Map.of("SLA 준수율", "100.0%", "평균 레이턴시", "56.6ms", "가용성", "99.99%");
        } else if (prompt.contains("파이프라인") || prompt.contains("장애") || prompt.contains("에러") || prompt.contains("healing")) {
            reply = "AI 파이프라인 자율 복구 에이전트가 최근 발생한 3건의 연계 에러(스키마 불일치, 네트워크 지연 등)를 **100% 자동 복구(총 265건 레코드 구제)** 완료하였습니다.";
            suggestedActions = List.of("파이프라인 복구 로그 확인", "DLQ 실패 큐 점검", "CDC 스트림 인스펙터");
            metricCards = Map.of("자율 복구율", "100%", "구제 레코드", "265건", "활성 장애", "0건");
        } else {
            reply = "안녕하세요! 전사 마스터 데이터 거버넌스 **AI Copilot**입니다. 품질 지표, SLA 준수율, 글로벌 멀티리전 동기화, 파이프라인 자율 복구 등 거버넌스 전반에 대해 무엇이든 질문해 주세요.";
            suggestedActions = List.of("전사 품질 현황 요약해줘", "SLA 계약 상태 점검해줘", "파이프라인 자율 복구 내역 알려줘");
            metricCards = Map.of("거버넌스 성숙도", "Level 5", "가동 기능", "50 / 50", "시스템 상태", "Online");
        }

        return GovernanceCopilotDto.CopilotChatResponse.builder()
                .reply(reply)
                .suggestedActions(suggestedActions)
                .metricCards(metricCards)
                .timestamp(now)
                .build();
    }
}
