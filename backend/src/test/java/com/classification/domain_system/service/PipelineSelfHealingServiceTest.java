package com.classification.domain_system.service;

import com.classification.domain_system.dto.PipelineSelfHealingDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.IntegrationLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PipelineSelfHealingServiceTest {

    @Mock
    private IntegrationChannelRepository channelRepository;

    @Mock
    private IntegrationLogRepository logRepository;

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private PipelineSelfHealingService healingService;

    @Test
    @DisplayName("getHealingReport: AI 기반 데이터 파이프라인 장애 진단 및 자율 복구 리포트 (DB 동적 조회)")
    void testGetHealingReport() {
        IntegrationChannel channel = new IntegrationChannel();
        channel.setId(UUID.randomUUID());
        channel.setName("ERP 연계 파이프라인");
        channel.setType("REST");
        channel.setActive(true);

        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());
        domain.setName(Map.of("ko", "임직원"));

        given(channelRepository.findAll()).willReturn(List.of(channel));
        given(domainRepository.findAll()).willReturn(List.of(domain));
        given(logRepository.findAll()).willReturn(Collections.emptyList());

        PipelineSelfHealingDto.PipelineHealingReport res = healingService.getHealingReport();

        assertThat(res).isNotNull();
        assertThat(res.getTotalIncidents()).isEqualTo(1);
        assertThat(res.getHealingSuccessRate()).isEqualTo(100.0);
        assertThat(res.getActions()).hasSize(1);
        assertThat(res.getActions().get(0).getPipelineChannel()).contains("ERP 연계 파이프라인");
        assertThat(res.getActions().get(0).getRecoveredCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("triggerHealing: 특정 파이프라인 채널 자율 복구 실행")
    void testTriggerHealing() {
        given(logRepository.findByStatus("FAIL")).willReturn(Collections.emptyList());
        boolean ok = healingService.triggerHealing("ERP 연계 파이프라인");
        assertThat(ok).isTrue();
    }
}
