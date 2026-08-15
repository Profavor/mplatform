package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataSlaContractDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSlaContractService {

    public DataSlaContractDto.DataSlaReport getSlaContracts() {
        List<DataSlaContractDto.DataSlaItem> items = new ArrayList<>();

        items.add(DataSlaContractDto.DataSlaItem.builder()
                .slaId("SLA-ERP-01")
                .contractName("ERP 실시간 재무/회계 마스터 SLA")
                .targetChannelOrDomain("SAP ERP / 제품·자재 도메인")
                .latencyThresholdMs(100)
                .currentLatencyMs(32)
                .availabilityTargetPercent(99.9)
                .currentAvailabilityPercent(99.99)
                .qualityCompliancePercent(99.8)
                .status("MEETING_SLA")
                .build());

        items.add(DataSlaContractDto.DataSlaItem.builder()
                .slaId("SLA-CRM-02")
                .contractName("CRM 고객/계정 실시간 스트리밍 SLA")
                .targetChannelOrDomain("Salesforce / 고객 도메인")
                .latencyThresholdMs(50)
                .currentLatencyMs(18)
                .availabilityTargetPercent(99.95)
                .currentAvailabilityPercent(99.98)
                .qualityCompliancePercent(99.4)
                .status("MEETING_SLA")
                .build());

        items.add(DataSlaContractDto.DataSlaItem.builder()
                .slaId("SLA-SCM-03")
                .contractName("글로벌 공급망 SCM 파트너 배치 SLA")
                .targetChannelOrDomain("SCM Gateway / 협력사 도메인")
                .latencyThresholdMs(500)
                .currentLatencyMs(120)
                .availabilityTargetPercent(99.5)
                .currentAvailabilityPercent(99.91)
                .qualityCompliancePercent(98.9)
                .status("MEETING_SLA")
                .build());

        long compliant = items.stream().filter(i -> "MEETING_SLA".equals(i.getStatus())).count();
        double rate = items.isEmpty() ? 100.0 : ((double) compliant / items.size()) * 100.0;

        return DataSlaContractDto.DataSlaReport.builder()
                .totalContracts(items.size())
                .compliantCount((int) compliant)
                .overallComplianceRate(rate)
                .contracts(items)
                .summary(String.format("전사 %d개 데이터 서비스 수준 협약(SLA)이 100%% 완벽 준수(평균 응답속도 56.6ms) 중입니다.", items.size()))
                .build();
    }
}
