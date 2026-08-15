package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataRetentionDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataRetentionPolicyServiceTest {

    @Mock private RecordRepository recordRepository;

    @InjectMocks
    private DataRetentionPolicyService dataRetentionPolicyService;

    private UUID domainId;
    private Record oldRecord;
    private Record newRecord;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();

        oldRecord = new Record();
        oldRecord.setId(UUID.randomUUID());
        oldRecord.setData("{\"name\":\"홍길동\",\"phone\":\"010-1111-2222\"}");
        oldRecord.setCreatedAt(LocalDateTime.now().minusYears(4));

        newRecord = new Record();
        newRecord.setId(UUID.randomUUID());
        newRecord.setData("{\"name\":\"이순신\",\"phone\":\"010-3333-4444\"}");
        newRecord.setCreatedAt(LocalDateTime.now().minusMonths(6));
    }

    @Test
    @DisplayName("scanExpiredRecords: 보존 연한 만료 레코드 탐색")
    void testScanExpiredRecords() {
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(oldRecord, newRecord));

        DataRetentionDto.ExpiredRecordScanResponse res = dataRetentionPolicyService.scanExpiredRecords(domainId, 3);

        assertThat(res).isNotNull();
        assertThat(res.getExpiredCount()).isEqualTo(1);
        assertThat(res.getExpiredRecordCodes()).hasSize(1);
    }

    @Test
    @DisplayName("purgeRecords: 만료 레코드 비식별화 파기 및 증명서 발급")
    void testPurgeRecords() {
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(oldRecord));

        DataRetentionDto.PurgeExecutionResponse res = dataRetentionPolicyService.purgeRecords(domainId, 3, "ANONYMIZE");

        assertThat(res).isNotNull();
        assertThat(res.getPurgedCount()).isEqualTo(1);
        assertThat(res.getCertificateId()).startsWith("CERT-");
    }
}
