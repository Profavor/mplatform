package com.classification.domain_system.service;

import com.classification.domain_system.dto.GovernanceMaturityDto;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.DqRuleRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.RecordRepository;
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
public class GovernanceMaturityServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private IntegrationChannelRepository channelRepository;

    @Mock
    private DqRuleRepository dqRuleRepository;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private GovernanceMaturityService maturityService;

    @BeforeEach
    void setUp() {
        given(domainRepository.count()).willReturn(5L);
        given(channelRepository.count()).willReturn(3L);
        given(dqRuleRepository.count()).willReturn(10L);
        given(recordRepository.count()).willReturn(1500L);
    }

    @Test
    @DisplayName("evaluateMaturity: 전사 거버넌스 5대 영역 성숙도 진단 리포트 (DB 동적 연동)")
    void testEvaluateMaturity() {
        GovernanceMaturityDto.GovernanceMaturityReport res = maturityService.evaluateMaturity();

        assertThat(res).isNotNull();
        assertThat(res.getOverallScore()).isGreaterThan(85);
        assertThat(res.getDimensions()).hasSize(5);
        assertThat(res.getKpiSummary()).isNotNull();
        assertThat(res.getKpiSummary()).containsKey("completeness");
    }
}
