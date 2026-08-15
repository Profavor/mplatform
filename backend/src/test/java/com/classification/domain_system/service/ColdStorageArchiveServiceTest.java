package com.classification.domain_system.service;

import com.classification.domain_system.dto.ColdStorageArchiveDto;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ColdStorageArchiveServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private ColdStorageArchiveService archiveService;

    @BeforeEach
    void setUp() {
        given(domainRepository.count()).willReturn(6L);
        given(recordRepository.count()).willReturn(25000L);
    }

    @Test
    @DisplayName("getArchives: 콜드스토리지 동결 패키지 목록 조회 (DB 동적 연동)")
    void testGetArchives() {
        List<ColdStorageArchiveDto.ArchivePackageInfo> list = archiveService.getArchives();

        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getDomainCount()).isEqualTo(6);
        assertThat(list.get(0).getStatus()).isEqualTo("FROZEN");
    }

    @Test
    @DisplayName("simulateDrRestore: DR 재해복구 시뮬레이션 복원 검증")
    void testSimulateDrRestore() {
        ColdStorageArchiveDto.DrSimulationResult res = archiveService.simulateDrRestore("PKG-001");

        assertThat(res).isNotNull();
        assertThat(res.isIntegrityVerified()).isTrue();
        assertThat(res.getDomainsRestored()).isEqualTo(6);
        assertThat(res.getRecordsRestored()).isEqualTo(25000);
        assertThat(res.getMessage()).contains("무결성 검증 100%");
    }
}
