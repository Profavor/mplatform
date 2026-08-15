package com.classification.domain_system.service;

import com.classification.domain_system.dto.MultiRegionConflictDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MultiRegionConflictServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private RecordRepository recordRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MultiRegionConflictService conflictService;

    @Test
    @DisplayName("getConflictReport: 멀티 리전 분산 동기화 충돌 탐지 및 자율 해소 리포트 (DB 동적 연동)")
    void testGetConflictReport() {
        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());
        domain.setName(Map.of("ko", "임직원"));

        Record record = new Record();
        record.setId(UUID.randomUUID());
        record.setData("{\"emp_name\":\"홍길동\",\"contact_phone\":\"010-1234-5678\"}");

        given(domainRepository.findAll()).willReturn(List.of(domain));
        given(recordRepository.findAllByDomainId(any())).willReturn(List.of(record));

        MultiRegionConflictDto.RegionSyncReport res = conflictService.getConflictReport();

        assertThat(res).isNotNull();
        assertThat(res.getTotalRegions()).isEqualTo(3);
        assertThat(res.getActiveConflicts()).isEqualTo(0);
        assertThat(res.getAutoResolvedCount()).isEqualTo(1);
        assertThat(res.getConflicts()).hasSize(1);
        assertThat(res.getConflicts().get(0).getResolvedValue()).isNotNull();
    }

    @Test
    @DisplayName("resolveConflict: 특정 충돌 건 수동 지정 해소")
    void testResolveConflict() {
        boolean ok = conflictService.resolveConflict("CONF-SYNC-001", "CUSTOM_VALUE");
        assertThat(ok).isTrue();
    }
}
