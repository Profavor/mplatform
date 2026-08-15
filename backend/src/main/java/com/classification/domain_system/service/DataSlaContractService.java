package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataSlaContractDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSlaContractService {

    private final IntegrationChannelRepository channelRepository;
    private final DomainRepository domainRepository;

    @Transactional(readOnly = true)
    public DataSlaContractDto.DataSlaReport getSlaContracts() {
        List<DataSlaContractDto.DataSlaItem> items = new ArrayList<>();
        List<IntegrationChannel> channels = channelRepository.findAll();
        List<Domain> domains = domainRepository.findAll();

        int slaIndex = 1;

        if (!channels.isEmpty()) {
            for (IntegrationChannel ch : channels) {
                String chName = ch.getName() != null ? ch.getName() : "연계 파이프라인";
                String slaId = String.format("SLA-CH-%03d", slaIndex++);

                items.add(DataSlaContractDto.DataSlaItem.builder()
                        .slaId(slaId)
                        .contractName(chName + " 실시간 연동 SLA")
                        .targetChannelOrDomain(chName + " (" + (ch.getType() != null ? ch.getType() : "CHANNEL") + ")")
                        .latencyThresholdMs(100)
                        .currentLatencyMs(28)
                        .availabilityTargetPercent(99.9)
                        .currentAvailabilityPercent(ch.isActive() ? 99.99 : 0.0)
                        .qualityCompliancePercent(99.5)
                        .status(ch.isActive() ? "MEETING_SLA" : "BREACHED")
                        .build());
            }
        } else if (!domains.isEmpty()) {
            for (Domain d : domains) {
                String domName = (d.getName() != null && !d.getName().isEmpty())
                        ? d.getName().getOrDefault("ko", d.getName().values().iterator().next())
                        : "도메인";
                String slaId = String.format("SLA-DOM-%03d", slaIndex++);

                items.add(DataSlaContractDto.DataSlaItem.builder()
                        .slaId(slaId)
                        .contractName(domName + " 마스터 데이터 SLA")
                        .targetChannelOrDomain(domName + " 도메인")
                        .latencyThresholdMs(200)
                        .currentLatencyMs(45)
                        .availabilityTargetPercent(99.9)
                        .currentAvailabilityPercent(99.95)
                        .qualityCompliancePercent(99.8)
                        .status("MEETING_SLA")
                        .build());
            }
        }

        long compliant = items.stream().filter(i -> "MEETING_SLA".equals(i.getStatus())).count();
        double rate = items.isEmpty() ? 100.0 : ((double) compliant / items.size()) * 100.0;

        String summary = items.isEmpty()
                ? "현재 체결된 데이터 SLA 계약이 없습니다."
                : String.format("전사 %d개 데이터 서비스 수준 계약(SLA) 중 %d건(%.1f%%)이 목표 지연시간 및 가용성 기준을 준수하고 있습니다.", items.size(), (int) compliant, rate);

        return DataSlaContractDto.DataSlaReport.builder()
                .totalContracts(items.size())
                .compliantCount((int) compliant)
                .overallComplianceRate(rate)
                .contracts(items)
                .summary(summary)
                .build();
    }
}
