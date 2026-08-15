package com.classification.domain_system.service;

import com.classification.domain_system.dto.GovernanceCopilotDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GovernanceCopilotServiceTest {

    private GovernanceCopilotService copilotService;

    @BeforeEach
    void setUp() {
        copilotService = new GovernanceCopilotService();
    }

    @Test
    @DisplayName("askCopilot: 데이터 품질 관련 거버넌스 질의 및 인텔리전트 추천 생성")
    void testAskCopilotQuality() {
        GovernanceCopilotDto.CopilotChatRequest req = GovernanceCopilotDto.CopilotChatRequest.builder()
                .prompt("전사 데이터 품질 현황 요약해줘")
                .build();

        GovernanceCopilotDto.CopilotChatResponse res = copilotService.askCopilot(req);

        assertThat(res).isNotNull();
        assertThat(res.getReply()).contains("96.8점");
        assertThat(res.getSuggestedActions()).isNotEmpty();
        assertThat(res.getMetricCards()).containsKeys("전사 DQ 점수", "완전성 지표");
    }

    @Test
    @DisplayName("askCopilot: SLA 관련 거버넌스 질의 응답")
    void testAskCopilotSla() {
        GovernanceCopilotDto.CopilotChatRequest req = GovernanceCopilotDto.CopilotChatRequest.builder()
                .prompt("SLA 지연시간 상태 알려줘")
                .build();

        GovernanceCopilotDto.CopilotChatResponse res = copilotService.askCopilot(req);

        assertThat(res).isNotNull();
        assertThat(res.getReply()).contains("100%");
        assertThat(res.getMetricCards()).containsKey("SLA 준수율");
    }
}
