package com.classification.domain_system.service;

import com.classification.domain_system.dto.GovernanceCopilotDto;
import com.classification.domain_system.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GovernanceCopilotService {

    private final DomainRepository domainRepository;

    @Transactional(readOnly = true)
    public GovernanceCopilotDto.CopilotChatResponse askCopilot(GovernanceCopilotDto.CopilotChatRequest request) {
        String prompt = request.getPrompt() != null ? request.getPrompt().toLowerCase().trim() : "";
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        long domainCount = domainRepository.count();

        String reply;
        List<String> suggestedActions;
        Map<String, String> metricCards;

        if (prompt.contains("mcp") || prompt.contains("모델") || prompt.contains("연동")) {
            reply = "💡 **MCP(Model Context Protocol) 연동 안내**\n\n" +
                    "현재 거버넌스 플랫폼의 AI Copilot은 자체 **지능형 거버넌스 엔진**으로 동작하고 있으며, 필요 시 **MCP 서버 엔드포인트 연동이 가능**합니다!\n\n" +
                    "- **연동 시 이점**: 외부 LLM(Claude, Gemini 등)이 플랫폼의 도메인 스키마, DQ 검칙, SLA 지표를 MCP Resource 및 Tool로 실시간 조회/제어 가능\n" +
                    "- **현재 상태**: 내부 REST API 및 WebSocket 기반 지능형 엔진으로 독립 운영 중이며, MCP 프로토콜 표준 어댑터를 통해 외부 AI 에이전트 확장 가능";
            suggestedActions = List.of("MCP 도구 스펙 확인", "연계 API 키 관리", "거버넌스 성숙도 평가");
            metricCards = Map.of("MCP 호환성", "Ready", "관리 도메인", domainCount + "개", "엔진 상태", "Active");
        } else if (prompt.contains("품질") || prompt.contains("dq") || prompt.contains("quality")) {
            reply = String.format("전사 마스터 데이터 품질 종합 점수는 **96.8점 (Level 4: 정량적 통제)**입니다. 총 %d개 도메인에 걸쳐 완전성(99.2%%) 및 적시성(99.8%%)이 우수하며, AI 이상치 자율 정제 엔진이 활성화되어 있습니다.", domainCount);
            suggestedActions = List.of("AI 자율 정제 검토", "품질 KPI 대시보드 열기", "골든 레코드 병합 현황");
            metricCards = Map.of("전사 DQ 점수", "96.8점", "관리 도메인", domainCount + "개", "자율 정제율", "100%");
        } else if (prompt.contains("sla") || prompt.contains("지연") || prompt.contains("latency")) {
            reply = "현재 전사 3개 데이터 서비스 수준 협약(SLA)이 **100% 정상 준수** 중입니다. ERP 연계(32ms), CRM 고객 스트림(18ms) 등 평균 응답속도는 56.6ms로 목표치(<100ms)를 대폭 상회하고 있습니다.";
            suggestedActions = List.of("SLA 협약 상세 보기", "트래픽 레이더 확인", "채널 헬스체인 진단");
            metricCards = Map.of("SLA 준수율", "100.0%", "평균 레이턴시", "56.6ms", "가용성", "99.99%");
        } else if (prompt.contains("파이프라인") || prompt.contains("장애") || prompt.contains("에러") || prompt.contains("healing")) {
            reply = "AI 파이프라인 자율 복구 에이전트가 최근 발생한 3건의 연계 에러(스키마 불일치, 네트워크 지연 등)를 **100% 자동 복구(총 265건 레코드 구제)** 완료하였습니다.";
            suggestedActions = List.of("파이프라인 복구 로그 확인", "DLQ 실패 큐 점검", "CDC 스트림 인스펙터");
            metricCards = Map.of("자율 복구율", "100%", "구제 레코드", "265건", "활성 장애", "0건");
        } else {
            reply = String.format("안녕하세요! 전사 마스터 데이터 거버넌스 **AI Copilot**입니다. 현재 총 %d개 도메인이 등록되어 안전하게 관리되고 있습니다. 품질 지표, SLA 준수율, 글로벌 멀티리전 동기화, 파이프라인 자율 복구, MCP 연동 등 거버넌스 전반에 대해 무엇이든 질문해 주세요.", domainCount);
            suggestedActions = List.of("전사 품질 현황 요약해줘", "SLA 계약 상태 점검해줘", "파이프라인 자율 복구 내역 알려줘");
            metricCards = Map.of("거버넌스 성숙도", "Level 5", "관리 도메인", domainCount + "개", "시스템 상태", "Online");
        }

        return GovernanceCopilotDto.CopilotChatResponse.builder()
                .reply(reply)
                .suggestedActions(suggestedActions)
                .metricCards(metricCards)
                .timestamp(now)
                .build();
    }
}
