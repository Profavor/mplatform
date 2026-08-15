package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataSlaContractDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import org.junit.jupiter.api.BeforeEach;
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
public class DataSlaContractServiceTest {

    @Mock
    private IntegrationChannelRepository channelRepository;

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private DataSlaContractService slaService;

    @BeforeEach
    void setUp() {
        IntegrationChannel channel = new IntegrationChannel();
        channel.setId(UUID.randomUUID());
        channel.setName("ERP 연계");
        channel.setType("REST");
        channel.setActive(true);

        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());
        domain.setName(Map.of("ko", "임직원"));

        given(channelRepository.findAll()).willReturn(List.of(channel));
        given(domainRepository.findAll()).willReturn(List.of(domain));
    }

    @Test
    @DisplayName("getSlaContracts: 전사 엔터프라이즈 데이터 SLA 계약 준수 리포트 (DB 동적 연동)")
    void testGetSlaContracts() {
        DataSlaContractDto.DataSlaReport res = slaService.getSlaContracts();

        assertThat(res).isNotNull();
        assertThat(res.getTotalContracts()).isEqualTo(1);
        assertThat(res.getCompliantCount()).isEqualTo(1);
        assertThat(res.getOverallComplianceRate()).isEqualTo(100.0);
        assertThat(res.getContracts()).hasSize(1);
        assertThat(res.getContracts().get(0).getStatus()).isEqualTo("MEETING_SLA");
    }
}
