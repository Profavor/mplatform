package com.classification.domain_system.service;

import com.classification.domain_system.dto.GovernanceCopilotDto;
import com.classification.domain_system.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class GovernanceCopilotServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private GovernanceCopilotService copilotService;

    @BeforeEach
    void setUp() {
        given(domainRepository.count()).willReturn(5L);
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
        assertThat(res.getMetricCards()).containsKeys("전사 DQ 점수", "관리 도메인");
    }

    @Test
    @DisplayName("askCopilot: MCP 연동 관련 거버넌스 질의 응답")
    void testAskCopilotMcp() {
        GovernanceCopilotDto.CopilotChatRequest req = GovernanceCopilotDto.CopilotChatRequest.builder()
                .prompt("이거 MCP 연동해야 하는거 아니냐?")
                .build();

        GovernanceCopilotDto.CopilotChatResponse res = copilotService.askCopilot(req);

        assertThat(res).isNotNull();
        assertThat(res.getReply()).contains("Model Context Protocol");
        assertThat(res.getMetricCards()).containsKey("MCP 호환성");
    }
}
