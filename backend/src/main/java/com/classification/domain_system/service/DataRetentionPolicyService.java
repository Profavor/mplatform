package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataRetentionDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataRetentionPolicyService {

    private final RecordRepository recordRepository;

    @Transactional(readOnly = true)
    public DataRetentionDto.ExpiredRecordScanResponse scanExpiredRecords(UUID domainId, int retentionYears) {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(retentionYears);
        List<Record> all = recordRepository.findAllByDomainId(domainId);

        List<Record> expired = all.stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());

        List<String> codes = expired.stream()
                .map(r -> "REC-" + r.getId().toString().substring(0, 8))
                .collect(Collectors.toList());

        return DataRetentionDto.ExpiredRecordScanResponse.builder()
                .domainId(domainId)
                .retentionYears(retentionYears)
                .expiredCount(expired.size())
                .expiredRecordCodes(codes)
                .summary(String.format("보존 연한(%d년)이 만료된 데이터 %d건이 탐지되었습니다.", retentionYears, expired.size()))
                .build();
    }

    @Transactional
    public DataRetentionDto.PurgeExecutionResponse purgeRecords(UUID domainId, int retentionYears, String purgeType) {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(retentionYears);
        List<Record> all = recordRepository.findAllByDomainId(domainId);

        List<Record> expired = all.stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());

        int count = expired.size();
        for (Record r : expired) {
            if ("HARD_DELETE".equalsIgnoreCase(purgeType)) {
                recordRepository.delete(r);
            } else {
                r.setData("{\"anonymized\":true,\"purgedAt\":\"" + LocalDateTime.now() + "\"}");
                r.setStatus("PURGED");
                recordRepository.save(r);
            }
        }

        String certId = "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return DataRetentionDto.PurgeExecutionResponse.builder()
                .domainId(domainId)
                .purgedCount(count)
                .certificateId(certId)
                .timestamp(LocalDateTime.now())
                .summary(String.format("총 %d건의 만료 데이터가 안전하게 파기 처리되었습니다 (증명서 번호: %s).", count, certId))
                .build();
    }
}
