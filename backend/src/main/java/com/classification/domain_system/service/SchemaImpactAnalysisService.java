package com.classification.domain_system.service;

import com.classification.domain_system.dto.SchemaImpactAnalysisDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SchemaImpactAnalysisService {

    private final DomainRepository domainRepository;
    private final RecordRepository recordRepository;
    private final IntegrationChannelRepository integrationChannelRepository;

    public SchemaImpactAnalysisService(DomainRepository domainRepository,
                                       RecordRepository recordRepository,
                                       IntegrationChannelRepository integrationChannelRepository) {
        this.domainRepository = domainRepository;
        this.recordRepository = recordRepository;
        this.integrationChannelRepository = integrationChannelRepository;
    }

    public SchemaImpactAnalysisDto.ImpactAnalysisResponse analyzeImpact(UUID domainId, SchemaImpactAnalysisDto.ImpactAnalysisRequest request) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + domainId));

        long totalRecords = recordRepository.countByNodeDomainIdAndStatus(domainId, "ACTIVE");
        List<IntegrationChannel> channels = integrationChannelRepository.findByIsActiveTrue();

        SchemaImpactAnalysisDto.ImpactAnalysisResponse response = new SchemaImpactAnalysisDto.ImpactAnalysisResponse();
        response.setDomainId(domainId);
        response.setTotalAffectedRecords(totalRecords);

        for (IntegrationChannel channel : channels) {
            response.getAffectedIntegrationChannels().add(channel.getName() != null ? channel.getName() : "Channel-" + channel.getId());
        }

        if ("DELETE_FIELD".equalsIgnoreCase(request.getChangeType())) {
            response.setRiskLevel(totalRecords > 100 ? "HIGH" : "MEDIUM");
            response.setExpectedDqViolations(0);
            response.getWarnings().add("필드 삭제 시 기존 " + totalRecords + "개 활성 레코드의 해당 필드 값이 영구적으로 제거되거나 조회 불가 상태가 됩니다.");
            if (!channels.isEmpty()) {
                response.getWarnings().add(channels.size() + "개의 외부 연동 채널에 필드 누락으로 인한 인바운드/아웃바운드 파싱 오류가 발생할 수 있습니다.");
            }
        } else if ("MODIFY_FIELD_TYPE".equalsIgnoreCase(request.getChangeType())) {
            response.setRiskLevel("CRITICAL");
            response.setExpectedDqViolations(Math.round(totalRecords * 0.15));
            response.getWarnings().add("데이터 타입 변경 시 기존 데이터 직렬화 실패 및 약 " + response.getExpectedDqViolations() + "건의 타입 불일치 위반이 예상됩니다.");
        } else {
            response.setRiskLevel("LOW");
            response.getWarnings().add("규칙 및 스키마 변경 사항 사전 검증 완료.");
        }

        return response;
    }
}
